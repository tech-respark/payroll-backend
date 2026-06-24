package com.relfor.pcs.payroll.util;

import com.relfor.pcs.payroll.dto.constants.BiometricApplicationNames;
import com.relfor.pcs.payroll.dto.constants.RegularizationRequestStatuses;
import com.relfor.pcs.payroll.entity.DayWiseAttendanceSummary;
import com.relfor.pcs.payroll.entity.PersonnelAttendance;
import com.relfor.pcs.payroll.projection.TenantStoreProjection;
import com.relfor.pcs.payroll.repository.DayWiseAttendanceSummaryRepository;
import com.relfor.pcs.payroll.repository.PersonnelAttendanceRepository;
import com.relfor.pcs.payroll.repository.TenantCompanyMappingRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AsyncAttendanceSummaryCalculation {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	TenantCompanyMappingRepository tenantCompanyMappingRepository;
	@Autowired
	PersonnelAttendanceRepository personnelAttendanceRepository;
	@Autowired
	DayWiseAttendanceSummaryRepository dayWiseAttendanceSummaryRepository;
	@PersistenceContext
	private EntityManager entityManager;

	@Async
	@Transactional
	public void asyncCalculateAttendanceSummaryForApprovalOrRejection(List<PersonnelAttendance> approvedOrRejectedPersonnelAttendanceList, Boolean isActualTimeBasedAttendance) {
		try {
			this.calculateAttendanceSummaryForApprovalOrRejection(approvedOrRejectedPersonnelAttendanceList, isActualTimeBasedAttendance);
		} catch (Exception ex) {
			logger.error("Exception inside async method calculateAttendanceSummaryForApproval: {}", ex.getMessage());
		} finally {
			MDC.clear();
		}
	}

	public void syncCalculateAttendanceSummaryForApprovalOrRejection(List<PersonnelAttendance> approvedOrRejectedPersonnelAttendanceList, Boolean isActualTimeBasedAttendance) {
		try {
			this.calculateAttendanceSummaryForApprovalOrRejection(approvedOrRejectedPersonnelAttendanceList, isActualTimeBasedAttendance);
		} catch (Exception ex) {
			logger.error("Exception inside sync method calculateAttendanceSummaryForApproval: {}", ex.getMessage());
		}
	}

	private void calculateAttendanceSummaryForApprovalOrRejection(List<PersonnelAttendance> approvedOrRejectedPersonnelAttendanceList, Boolean isActualTimeBasedAttendance) {
		Long tenantId = approvedOrRejectedPersonnelAttendanceList.get(0).getTenantId();
		Long storeId = approvedOrRejectedPersonnelAttendanceList.get(0).getStoreId();

		String timeZone = null;
		if (isActualTimeBasedAttendance == null) {
			Optional<TenantStoreProjection> tenantStoreProjectionOptional =
					tenantCompanyMappingRepository.getTenantStoreMapping(tenantId, storeId,
							BiometricApplicationNames.RESPARK.name());
			isActualTimeBasedAttendance = tenantStoreProjectionOptional
					.map(TenantStoreProjection::getIsActualTimeBasedAttendance)
					.orElse(false);
			timeZone = tenantStoreProjectionOptional
					.map(TenantStoreProjection::getTimeZone)
					.orElse(null);
		}
		ZoneId zoneId = !StringUtils.isEmpty(timeZone) ? ZoneId.of(timeZone) : ZoneId.systemDefault();
		List<List<PersonnelAttendance>> subdividedLists = new ArrayList<>();
		this.findAndFilterAndSubdivideAttendanceList(approvedOrRejectedPersonnelAttendanceList,
				tenantId, storeId, subdividedLists);
		if (!subdividedLists.isEmpty()) {
			logger.info("Number of lists for unique combination of staffId and attendance date: {}", subdividedLists.size());
			List<DayWiseAttendanceSummary> dayWiseAttendanceSummaryList = new ArrayList<>();
			for (List<PersonnelAttendance> attendanceList: subdividedLists) {
				try {
					this.calculateAndAddToAttendanceSummaryList(tenantId, storeId,
							attendanceList.get(0).getPersonnelId(),
							attendanceList.get(0).getAttendanceDate(),
							isActualTimeBasedAttendance, attendanceList,
							dayWiseAttendanceSummaryList, zoneId);
				} catch (Exception ex) {
					logger.error("Exception while calculating dayWiseAttendanceSummary for Tenant: {}, Store: {}, Staff: {}, and Attendance Date: {}, Exception: {}",
							tenantId,
							storeId,
							attendanceList.get(0).getPersonnelId(),
							attendanceList.get(0).getAttendanceDate(),
							ex.getMessage());
				}
			}
			if (!dayWiseAttendanceSummaryList.isEmpty()) {
				logger.info("Total Number of day wise summaries saved for TenantId: {} is {}",
						dayWiseAttendanceSummaryList.get(0).getTenantId(),
						dayWiseAttendanceSummaryList.size());
//				dayWiseAttendanceSummaryRepository.saveAll(dayWiseAttendanceSummaryList);
			}
		}
	}

	private void findAndFilterAndSubdivideAttendanceList(List<PersonnelAttendance> personnelAttendanceList,
														 Long tenantId, Long storeId,
														 List<List<PersonnelAttendance>> subdividedLists) {
		//personnelAttendanceList is the list of approved requests saved in DB

		// Sort the attendance list by personnelId and attendanceDate.
		personnelAttendanceList.sort(Comparator
				.comparing(PersonnelAttendance::getPersonnelId)
				.thenComparing(PersonnelAttendance::getAttendanceDate)
				.thenComparing(PersonnelAttendance::getPunchTimestamp));


		//personnelAttendanceListFromDb is the list of all the entries in attendance table (for the combination of personnel and attendance date) for which new requests got approved
		List<PersonnelAttendance> personnelAttendanceListFromDb = this.findPersonnelAttendanceListFromDbUsingJpql(
				personnelAttendanceList, tenantId, storeId
		);
		if (!personnelAttendanceListFromDb.isEmpty()) {
			// Sort the attendance list by personnelId and attendanceDate.
			personnelAttendanceListFromDb.sort(Comparator
					.comparing(PersonnelAttendance::getPersonnelId)
					.thenComparing(PersonnelAttendance::getAttendanceDate)
					.thenComparing(PersonnelAttendance::getPunchTimestamp));

			// Subdivide the list into multiple lists based on personnelId and attendanceDate.
			List<PersonnelAttendance> currentSublist = new ArrayList<>();
			for (PersonnelAttendance attendance : personnelAttendanceListFromDb) {
				// If the sublist is empty or matches the current item's grouping, add it.
				if (currentSublist.isEmpty() || (
						attendance.getPersonnelId().equals(currentSublist.get(0).getPersonnelId()) &&
								attendance.getAttendanceDate().equals(currentSublist.get(0).getAttendanceDate())
				)) {
					currentSublist.add(attendance);
				} else {
					// Save the current sublist and start a new one.
					subdividedLists.add(new ArrayList<>(currentSublist));
					currentSublist.clear();
					currentSublist.add(attendance);
				}
			}
			// Add the last sublist if not empty.
			if (!currentSublist.isEmpty()) {
				subdividedLists.add(currentSublist);
			}
		}
	}

	private List<PersonnelAttendance> findPersonnelAttendanceListFromDbUsingJpql(List<PersonnelAttendance> personnelAttendanceList,
															Long tenantId, Long storeId) {
		StringBuilder jpqlBuilder = new StringBuilder("SELECT p FROM PersonnelAttendance p WHERE " +
				"p.currentStatus = :currentStatus AND " +
				"p.tenantId = :tenantId AND p.storeId = :storeId AND (");
		List<String> personnelAndDatesConditions = new ArrayList<>();
		int index = 0;
		Map<String, Object> personnelAndDatesParameters = new HashMap<>();

		Long personnelId = null;
		List<LocalDate> attendanceDates = new ArrayList<>();
		String personnelParam = null;
		String dateParam = null;
		for (int i = 0; i < personnelAttendanceList.size(); i++) {
			PersonnelAttendance personnelAttendance = personnelAttendanceList.get(i);
			Long currentCode = personnelAttendance.getPersonnelId();

			// First iteration or new personnelId detected
			if (i == 0 || !currentCode.equals(personnelId)) {
				// Save the previous personnel data before switching to a new one
				if (i > 0) {
					personnelAndDatesConditions.add("(p.personnelId = :" + personnelParam + " AND p.attendanceDate IN (:" + dateParam + "))");
					personnelAndDatesParameters.put(personnelParam, personnelId);
					personnelAndDatesParameters.put(dateParam, new ArrayList<>(attendanceDates));
				}

				// Initialize new personnel tracking
				personnelId = currentCode;
				personnelParam = "personnelId" + index;
				dateParam = "attendanceDates" + index;
				attendanceDates = new ArrayList<>();
				index++;
			}

			// Add unique attendance date
			if (!attendanceDates.contains(personnelAttendance.getAttendanceDate())) {
				attendanceDates.add(personnelAttendance.getAttendanceDate());
			}
		}

		//Save the last personnel's data
		if (personnelId != null) {
			personnelAndDatesConditions.add("(p.personnelId = :" + personnelParam + " AND p.attendanceDate IN (:" + dateParam + "))");
			personnelAndDatesParameters.put(personnelParam, personnelId);
			personnelAndDatesParameters.put(dateParam, attendanceDates);
		}

		jpqlBuilder.append(String.join(" OR ", personnelAndDatesConditions)).append(")");

		TypedQuery<PersonnelAttendance> query = entityManager.createQuery(jpqlBuilder.toString(), PersonnelAttendance.class);
		query.setParameter("currentStatus", RegularizationRequestStatuses.APPROVED.name());
		query.setParameter("tenantId", tenantId);
		query.setParameter("storeId", storeId);

		// Set dynamically created parameters
		for (Map.Entry<String, Object> param : personnelAndDatesParameters.entrySet()) {
			query.setParameter(param.getKey(), param.getValue());
		}

		List<PersonnelAttendance> personnelAttendanceListFromDb = query.getResultList();

		return personnelAttendanceListFromDb;
	}

	private void calculateAndAddToAttendanceSummaryList(Long tenantId, Long storeId,
														Long personnelId, LocalDate dateOfAttendance,
														boolean isActualTimeBasedAttendance,
														List<PersonnelAttendance> attendanceList,
														List<DayWiseAttendanceSummary> dayWiseAttendanceSummaryList,
														ZoneId zoneId) {
		logger.debug("Iteration of calculateAndAddToAttendanceSummaryList for tenantId: {}, storeId: {}, Staff: {} and Attendance Date: {}",
				tenantId, storeId, personnelId, dateOfAttendance);
		if (attendanceList.size() >= 2) {
			BigDecimal sumOfActualHoursWorkedInADay = BigDecimal.ZERO;
			BigDecimal totalBreakTimeInADay = BigDecimal.ZERO;

			Instant firstCheckin = attendanceList.get(0).getPunchTimestamp();
			Instant lastCheckout = attendanceList.get(attendanceList.size() - 1).getPunchTimestamp();
			Duration totalWorkDuration = Duration.between(firstCheckin, lastCheckout);
			BigDecimal totalHoursWorkedInADay = BigDecimal.valueOf(totalWorkDuration.toMinutes()).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

			for (int i = 0; i < attendanceList.size()-1; i++) {
				if ((i+1) % 2 != 0) {
					Duration workDuration = Duration.between(
							attendanceList.get(i).getPunchTimestamp(),
							attendanceList.get(i+1).getPunchTimestamp());
					sumOfActualHoursWorkedInADay = sumOfActualHoursWorkedInADay.add(BigDecimal.valueOf(workDuration.toMinutes()).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP));
				} else {
					Duration breakDuration = Duration.between(
							attendanceList.get(i).getPunchTimestamp(),
							attendanceList.get(i+1).getPunchTimestamp());
					totalBreakTimeInADay = totalBreakTimeInADay.add(BigDecimal.valueOf(breakDuration.toMinutes()).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP));
				}
			}
			this.addUpdatedOrNewAttendanceSummaryToList(tenantId, storeId, personnelId, dateOfAttendance,
					totalHoursWorkedInADay, sumOfActualHoursWorkedInADay, totalBreakTimeInADay,dayWiseAttendanceSummaryList,
					firstCheckin, lastCheckout, zoneId);
		}
	}

	private void addUpdatedOrNewAttendanceSummaryToList(Long tenantId, Long storeId,
														Long personnelId, LocalDate dateOfAttendance,
														BigDecimal totalHoursWorkedInADay,
														BigDecimal sumOfActualHoursWorkedInADay,
														BigDecimal totalBreakTimeInADay,
														List<DayWiseAttendanceSummary> dayWiseAttendanceSummaryList,
														Instant firstCheckin, Instant lastCheckout, ZoneId zoneId) {
		Optional<DayWiseAttendanceSummary> summaryOptional = dayWiseAttendanceSummaryRepository
				.findByTenantIdAndStoreIdAndPersonnelIdAndApplicationNameAndAttendanceDate(
						tenantId, storeId, personnelId,
						BiometricApplicationNames.RESPARK.name(),
						dateOfAttendance);
		LocalTime firstCheckinTime = firstCheckin.atZone(zoneId).toLocalTime();
		LocalTime lastCheckoutTime = lastCheckout.atZone(zoneId).toLocalTime();

		DayWiseAttendanceSummary summary;
		if (summaryOptional.isPresent()) {
			summary = summaryOptional.get();
			summary.setIsPresent(true);
			summary.setIsAbsent(false);
			summary.setIsPenaltyAbsent(false);
			if (summary.getIsWeeklyOff()) {
				summary.setIsExtraDay(true);
			} else {
				summary.setIsExtraDay(false);
			}
			if (!ObjectUtils.isEmpty(summary.getFirstCheckinPerRoster())
					&& !ObjectUtils.isEmpty(summary.getLastCheckoutPerRoster())
					&& !ObjectUtils.isEmpty(summary.getWorkingHoursAsPerRoster())) {
				long lateArrivalMinutes = Duration.between(summary.getFirstCheckinPerRoster(), firstCheckinTime).toMinutes();
				long earlyExitMinutes = Duration.between(lastCheckoutTime, summary.getLastCheckoutPerRoster()).toMinutes();

				if (lateArrivalMinutes > 0) {
					summary.setIsLateArrival(true);
					summary.setLateArrivalMins(lateArrivalMinutes);
				} else {
					summary.setIsLateArrival(false);
					summary.setLateArrivalMins(0L);
				}
				if (earlyExitMinutes > 0) {
					summary.setIsEarlyExit(true);
					summary.setEarlyExitMins(earlyExitMinutes);
				} else {
					summary.setIsEarlyExit(false);
					summary.setEarlyExitMins(0L);
				}
				summary.setDiffBetweenActualAndRosterWorkMins(
						totalHoursWorkedInADay
								.subtract(summary.getWorkingHoursAsPerRoster())
								.multiply(BigDecimal.valueOf(60))
								.setScale(0, RoundingMode.HALF_UP)
								.longValue());
			}
		} else {
			summary = new DayWiseAttendanceSummary();
			summary.setTenantId(tenantId);
			summary.setStoreId(storeId);
			summary.setPersonnelId(personnelId);
			summary.setApplicationName(BiometricApplicationNames.RESPARK.name());
			summary.setAttendanceDate(dateOfAttendance);
			summary.setAttendanceDayOfWeek(dateOfAttendance.getDayOfWeek().name());

			summary.setIsOnLeave(false);
			summary.setIsWeeklyOff(false);
			summary.setIsPresent(true);
			summary.setIsAbsent(false);
			summary.setIsPenaltyAbsent(false);
			summary.setIsExtraDay(true);
			summary.setIsLateArrival(false);
			summary.setIsEarlyExit(false);
		}
		summary.setTotalHoursWorkedInADay(totalHoursWorkedInADay);
		summary.setSumOfActualHoursWorkedInADay(sumOfActualHoursWorkedInADay);
		summary.setTotalBreakTimeInADay(totalBreakTimeInADay);

		dayWiseAttendanceSummaryRepository.save(summary);
		dayWiseAttendanceSummaryList.add(summary);
	}
}