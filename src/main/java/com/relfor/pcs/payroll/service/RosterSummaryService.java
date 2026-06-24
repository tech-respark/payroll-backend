package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.constants.BiometricApplicationNames;
import com.relfor.pcs.payroll.entity.DayWiseAttendanceSummary;
import com.relfor.pcs.payroll.entity.SStaffShifts;
import com.relfor.pcs.payroll.entity.StaffBreakTime;
import com.relfor.pcs.payroll.projection.TenantStoreProjection;
import com.relfor.pcs.payroll.repository.DayWiseAttendanceSummaryRepository;
import com.relfor.pcs.payroll.repository.TenantCompanyMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RosterSummaryService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	DayWiseAttendanceSummaryRepository dayWiseAttendanceSummaryRepository;
	@Autowired
	TenantCompanyMappingRepository tenantCompanyMappingRepository;

	public void addRosterSummaryToAttendanceSummary(List<SStaffShifts> staffShiftsList) {
		try {
			Long tenantId = staffShiftsList.get(0).getTenantId();
			Long storeId = staffShiftsList.get(0).getStoreId();

			Optional<TenantStoreProjection> tenantStoreProjectionOptional =
					tenantCompanyMappingRepository.getTenantStoreMapping(tenantId, storeId,
							BiometricApplicationNames.RESPARK.name());
			boolean isPaidLeaveApplicable = tenantStoreProjectionOptional
					.map(TenantStoreProjection::getIsPaidLeaveApplicable)
					.orElse(false);
			String penaltyDaysString = tenantStoreProjectionOptional
					.map(TenantStoreProjection::getPenaltyAbsentDays)
					.orElse(null);

			Set<String> penaltyDays = Optional.ofNullable(penaltyDaysString)
					.map(days -> Arrays.stream(days.split(","))
							.map(String::trim)
							.map(String::toLowerCase)
							.collect(Collectors.toSet()))
					.orElse(Collections.emptySet());

			List<Long> personnelIds = staffShiftsList.stream()
					.map(SStaffShifts::getStaffId)
					.distinct()
					.collect(Collectors.toList());

			List<LocalDate> attendanceDates = staffShiftsList.stream()
					.map(shift -> shift.getShiftDate() != null ? shift.getShiftDate() : null)
					.filter(Objects::nonNull)
					.distinct()
					.collect(Collectors.toList());

			List<DayWiseAttendanceSummary> existingSummaries = dayWiseAttendanceSummaryRepository.findExistingDayWiseAttendanceSummaries(
					personnelIds,
					attendanceDates,
					tenantId,
					storeId
			);

			Map<String, DayWiseAttendanceSummary> summaryMap = existingSummaries.stream()
					.collect(Collectors.toMap(
							summary -> summary.getTenantId() + "_" +
									summary.getStoreId() + "_" +
									summary.getApplicationName() + "_" +
									summary.getPersonnelId() + "_" +
									summary.getAttendanceDate(),
							summary -> summary
					));

			Map<String, DayWiseAttendanceSummary> dayWiseSummaryToBeSavedMap = new LinkedHashMap<>();
			for (SStaffShifts staffShift: staffShiftsList) {
				String key = staffShift.getTenantId() + "_" +
						staffShift.getStoreId() + "_" +
						BiometricApplicationNames.RESPARK.name() + "_" +
						staffShift.getStaffId() + "_" +
						(staffShift.getShiftDate() != null ? staffShift.getShiftDate().toString() : "");
				DayWiseAttendanceSummary dayWiseAttendanceSummary = summaryMap.get(key);
				if (dayWiseAttendanceSummary == null) {
					dayWiseAttendanceSummary = new DayWiseAttendanceSummary();
					dayWiseAttendanceSummary.setTenantId(staffShift.getTenantId());
					dayWiseAttendanceSummary.setStoreId(staffShift.getStoreId());
					dayWiseAttendanceSummary.setApplicationName(BiometricApplicationNames.RESPARK.name());
					dayWiseAttendanceSummary.setPersonnelId(staffShift.getStaffId());
					dayWiseAttendanceSummary.setAttendanceDate(staffShift.getShiftDate());
					dayWiseAttendanceSummary.setAttendanceDayOfWeek(dayWiseAttendanceSummary.getAttendanceDate().getDayOfWeek().toString());
				}

				// Calculate hoursAsPerRoster
				try {
					String[] timeParts = staffShift.getSlot().split("-");
					if (timeParts.length == 2) {
						LocalTime start = LocalTime.parse(timeParts[0]);
						LocalTime end = LocalTime.parse(timeParts[1]);
						long minutes = Duration.between(start, end).toMinutes();
						dayWiseAttendanceSummary.setFirstCheckinPerRoster(start);
						dayWiseAttendanceSummary.setLastCheckoutPerRoster(end);
						dayWiseAttendanceSummary.setWorkingHoursAsPerRoster(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
					}
				} catch (Exception e) {
					logger.error("Exception while calculating hoursAsPerRoster for staff & date: {}", key);
				}

				// Calculate breakTimeAsPerRoster (from breakHours in minutes)
				int totalBreakMinutes = staffShift.getStaffBreakTime() != null
						? staffShift.getStaffBreakTime().stream()
						.filter(b -> b.getBreakHours() != null)
						.mapToInt(b -> b.getBreakHours().intValue())
						.sum()
						: 0;

				if (staffShift.getWeeklyOff()) {
					dayWiseAttendanceSummary.setIsWeeklyOff(true);
					dayWiseAttendanceSummary.setIsOnLeave(false);
					dayWiseAttendanceSummary.setIsAbsent(false);
					dayWiseAttendanceSummary.setIsPenaltyAbsent(false);
				} else if (staffShift.getOnLeave()) {
					dayWiseAttendanceSummary.setIsWeeklyOff(false);
					dayWiseAttendanceSummary.setIsAbsent(true);
					if (isPaidLeaveApplicable) {
						dayWiseAttendanceSummary.setIsOnLeave(true);
						dayWiseAttendanceSummary.setIsAbsent(false);
						dayWiseAttendanceSummary.setIsPenaltyAbsent(false);
					} else {
						dayWiseAttendanceSummary.setIsOnLeave(false);
						if (penaltyDays.contains(dayWiseAttendanceSummary.getAttendanceDayOfWeek().toLowerCase())) {
							dayWiseAttendanceSummary.setIsAbsent(false);
							dayWiseAttendanceSummary.setIsPenaltyAbsent(true);
						} else {
							dayWiseAttendanceSummary.setIsAbsent(true);
							dayWiseAttendanceSummary.setIsPenaltyAbsent(false);
						}
					}
				} else {
					dayWiseAttendanceSummary.setIsWeeklyOff(false);
					dayWiseAttendanceSummary.setIsOnLeave(false);
					dayWiseAttendanceSummary.setIsAbsent(true);
					dayWiseAttendanceSummary.setIsPenaltyAbsent(false);
				}
				dayWiseAttendanceSummary.setIsExtraDay(false);
				dayWiseAttendanceSummary.setIsLateArrival(false);
				dayWiseAttendanceSummary.setIsEarlyExit(false);
				dayWiseAttendanceSummary.setBreakHoursAsPerRoster(BigDecimal.valueOf(totalBreakMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));

				dayWiseSummaryToBeSavedMap.putIfAbsent(key, dayWiseAttendanceSummary);
			}

			if (!dayWiseSummaryToBeSavedMap.isEmpty()) {
				List<DayWiseAttendanceSummary> dayWiseAttendanceSummaryListToBeSaved = new ArrayList<>(dayWiseSummaryToBeSavedMap.values());
				if (!dayWiseAttendanceSummaryListToBeSaved.isEmpty()) {
					dayWiseAttendanceSummaryRepository.saveAll(dayWiseAttendanceSummaryListToBeSaved);
				}
			}
		} catch (Exception ex) {
			logger.error("Exception inside addRosterSummaryToAttendanceSummary: {}", ex.getMessage());
		}
	}

	public void addStaffBreakTimeToSummary(List<StaffBreakTime> staffBreakTimeList, List<SStaffShifts> staffShiftsList) {
		try {
			staffBreakTimeList.sort(Comparator.comparing(StaffBreakTime::getStaffId).thenComparing(StaffBreakTime::getStaffShiftId));
			Long staffId = null;
			Long staffShiftId = null;
			for (StaffBreakTime staffBreak: staffBreakTimeList) {
				if ((staffId == null && staffShiftId == null)
						|| (!Objects.equals(staffId, staffBreak.getStaffId()) || !Objects.equals(staffShiftId, staffBreak.getStaffShiftId()))) {
					staffId = staffBreak.getStaffId();
					staffShiftId = staffBreak.getStaffShiftId();

					Optional<SStaffShifts> sStaffShiftsOptional = staffShiftsList.stream().filter(shift -> Objects.equals(shift.getId(), staffBreak.getStaffShiftId())).findFirst();
					if (sStaffShiftsOptional.isPresent()) {
						Optional<DayWiseAttendanceSummary> dayWiseAttendanceSummaryOptional =
								dayWiseAttendanceSummaryRepository.findByPersonnelIdAndAttendanceDate(staffBreak.getStaffId(), sStaffShiftsOptional.get().getShiftDate());

						if (dayWiseAttendanceSummaryOptional.isPresent()) {
							// Calculate breakTimeAsPerRoster (from breakHours in minutes)
							int totalBreakMinutes = sStaffShiftsOptional.get().getStaffBreakTime() != null
									? sStaffShiftsOptional.get().getStaffBreakTime().stream()
									.filter(b -> b.getBreakHours() != null)
									.mapToInt(b -> b.getBreakHours().intValue())
									.sum()
									: 0;

							dayWiseAttendanceSummaryOptional.get().setBreakHoursAsPerRoster(BigDecimal.valueOf(totalBreakMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
							dayWiseAttendanceSummaryRepository.save(dayWiseAttendanceSummaryOptional.get());
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Exception inside addStaffBreakTimeToSummary: {}", ex.getMessage());
		}
	}
}