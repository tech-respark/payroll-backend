package com.relfor.pcs.payroll.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.relfor.pcs.payroll.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;


import com.relfor.pcs.payroll.dto.constants.BiometricApplicationNames;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.repository.*;
import com.relfor.pcs.payroll.repository.StoreProfileConfigRepository;
import com.relfor.pcs.payroll.entity.StoreProfileConfig;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;



@Service
public class StaffShiftsService {

	final long SINGLE_DAY_TIMESTAMP = 60 * 60 * 24;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
	static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	@Autowired
	StaffBreakTimeService staffBreakTimeSvc;
	@Autowired
	StaffBreakTimeRepository staffBreakTimeRepo;
	@Autowired
	private SStaffShiftsRepository staffShiftsRepository;
	// @Autowired
	// ApiHelper apiHelper;
	@Autowired
	StoreProfileConfigRepository storeProfileConfigRepository;
	@Autowired
	StaffBookedRepository staffBookedRepo;
	@Autowired
	SShiftsSlotsRepository shiftSlotRepo;
	@Autowired
	RosterSummaryService rosterSummaryService;
	@Autowired
	PersonnelDetailsRepository personnelDetailsRepository;
	@Autowired
	StoreDetailsRepository storeDetailsRepository;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	AttendanceManagementService attendanceManagementService;

	public List<SStaffShifts> createStaffShiftNewInput(CreateStaffShiftInput createStaffShiftInput) {

		List<SStaffShifts> result = new ArrayList<>();
		try {
			if (createStaffShiftInput.getTenantId() != null && createStaffShiftInput.getStoreId() != null
					&& createStaffShiftInput.getStartDate() != null && createStaffShiftInput.getNoOfDays() != null
					&& createStaffShiftInput.getStaffShiftsList() != null
					&& createStaffShiftInput.getStaffShiftsList().size() > 0) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				DateTimeFormatter formatterForDay = DateTimeFormatter.ofPattern("EEEE");

//				Instant startDateInstant = Instant.parse(createStaffShiftInput.getStartDate().concat("T00:00:00.000Z"));
//				Instant endDateInstant = startDateInstant.plus(createStaffShiftInput.getNoOfDays(), ChronoUnit.DAYS);

				java.time.LocalDate startDate = java.time.LocalDate.parse(createStaffShiftInput.getStartDate());
				java.time.LocalDate endDate = startDate.plusDays(createStaffShiftInput.getNoOfDays());

//				String endDate = new SimpleDateFormat("yyyy-MM-dd").format(Date.from(endDateInstant));

//				List<Date> dates = getDatesBetweenTwoDates(Date.from(startDateInstant), Date.from(endDateInstant));

				List<java.time.LocalDate> dates = Stream.iterate(startDate, date -> !date.isAfter(endDate), date -> date.plusDays(1))
						.collect(Collectors.toList());
				List<SStaffShifts> updatedStaffShiftList = new ArrayList<>();

				SShiftsSlots shiftSlots = null;
				if (!ObjectUtils.isEmpty(createStaffShiftInput.getShiftSlotId())) {
					Optional<SShiftsSlots> shiftSlot = shiftSlotRepo.findById(createStaffShiftInput.getShiftSlotId());
					if (!ObjectUtils.isEmpty(shiftSlot)) {
						shiftSlots = shiftSlot.get();
					}
				}

				Map<String, String> dayMap = new HashMap<>();
				dayMap.put("Sunday", "SUN");
				dayMap.put("Monday", "MON");
				dayMap.put("Tuesday", "TUE");
				dayMap.put("Wednesday", "WED");
				dayMap.put("Thursday", "THU");
				dayMap.put("Friday", "FRI");
				dayMap.put("Saturday", "SAT");

				boolean deleteBreakTimeOrphans = false;

				// BULK FETCHING TO FIX N+1 QUERIES
				List<Long> allStaffIds = createStaffShiftInput.getStaffShiftsList().stream()
						.map(SStaffShifts::getStaffId).collect(Collectors.toList());

				List<SStaffShifts> allExistingShiftsList = new ArrayList<>();
				Map<Long, List<SStaffShifts>> existingShiftsMap = new HashMap<>();
				if (!allStaffIds.isEmpty()) {
					allExistingShiftsList = staffShiftsRepository.findByTenantIdAndStoreIdAndStaffIdInAndShiftDate(
							createStaffShiftInput.getTenantId(), createStaffShiftInput.getStoreId(),
							allStaffIds, createStaffShiftInput.getStartDate(), endDate.format(formatter));
					existingShiftsMap = allExistingShiftsList.stream()
							.collect(Collectors.groupingBy(SStaffShifts::getStaffId));
				}

				List<PersonnelDetails> allPersonnelDetailsList = new ArrayList<>();
				Map<Long, PersonnelDetails> personnelMap = new HashMap<>();
				if (!allStaffIds.isEmpty()) {
					allPersonnelDetailsList = personnelDetailsRepository.getPersonnelById(allStaffIds);
					personnelMap = allPersonnelDetailsList.stream()
							.collect(Collectors.toMap(PersonnelDetails::getId, p -> p));
				}

				// THIS LOOP RUN TO COVER EVERY STAFF
				for (SStaffShifts staffShift : createStaffShiftInput.getStaffShiftsList()) {
					List<StaffBreakTime> incomingStaffBreakTimeList = staffShift.getStaffBreakTime();
					if (!ObjectUtils.isEmpty(incomingStaffBreakTimeList)) {
						deleteBreakTimeOrphans = true;
						for (StaffBreakTime staffBreakTime: incomingStaffBreakTimeList) {
							this.calculateBreakTime(staffShift, staffBreakTime);
						}
					}
					List<String> weekOff = new ArrayList<>();
					List<SStaffShifts> existingStaffShiftList = existingShiftsMap.getOrDefault(staffShift.getStaffId(), new ArrayList<>());
					PersonnelDetails personnelDetails = personnelMap.get(staffShift.getStaffId());
					
					if (personnelDetails != null && !StringUtils.isEmpty(personnelDetails.getWeeklyOff())) {
						weekOff = new ArrayList<>(Arrays.asList(personnelDetails.getWeeklyOff().split(",")));
					}
					// UPDATE EXISTING STAFF-SHIFT DETAILS
					boolean isThatDay = true;
					for (SStaffShifts existingStaffShift : existingStaffShiftList) {
						boolean isPaylodeContainsWeekOff = false;
						for (String weekDay : weekOff) {
							if (weekDay.equalsIgnoreCase(existingStaffShift.getDay())) {
								isPaylodeContainsWeekOff = true;
								break;
							}
						}
						existingStaffShift.setWeeklyOff(isPaylodeContainsWeekOff);

						if (!ObjectUtils.isEmpty(shiftSlots)
								&& !ObjectUtils.isEmpty(shiftSlots.getDayWiseShiftsTiming())) {
							String staffShiftDay = dayMap.getOrDefault(
									staffShift.getDay().substring(0, 3).toUpperCase(),
									staffShift.getDay().substring(0, 3).toUpperCase());

							String shiftDay = dayMap.getOrDefault(
									existingStaffShift.getDay().substring(0, 3).toUpperCase(),
									existingStaffShift.getDay().substring(0, 3).toUpperCase());

							for (DayWiseShiftsTiming dayWiseTiming : shiftSlots.getDayWiseShiftsTiming()) {
								String dayWiseTimingDay = dayMap.getOrDefault(
										dayWiseTiming.getDay().substring(0, 3).toUpperCase(),
										dayWiseTiming.getDay().substring(0, 3).toUpperCase());

								if (shiftDay.equalsIgnoreCase(dayWiseTimingDay) && !isThatDay) {
									existingStaffShift.setSlot(
											dayWiseTiming.getStartTime() + "-" + dayWiseTiming.getClosureTime());
								} else if (staffShiftDay.equalsIgnoreCase(dayWiseTimingDay) && isThatDay) {
									if (staffShift.getSlot().equalsIgnoreCase(
											dayWiseTiming.getStartTime() + "-" + dayWiseTiming.getClosureTime())) {
										existingStaffShift.setSlot(staffShift.getSlot());
									}
									isThatDay = false;
								}
							}

						} else {
//							String slot = shiftSlots.getStartTime() + "-" + shiftSlots.getEndTime();
//							if (staffShift.getSlot().equalsIgnoreCase(slot)) {
//								existingStaffShift.setSlot(staffShift.getSlot());
//							}
							existingStaffShift.setSlot(staffShift.getSlot());
						}

//						Date changeDate = new SimpleDateFormat("yyyy-MM-dd")
//								.parse(new SimpleDateFormat("yyyy-MM-dd").format(staffShift.getShiftDate()));
						java.time.LocalDate changeDate = staffShift.getShiftDate();
						for (int i = 0; i <= createStaffShiftInput.getNoOfDays(); i++) {
//							for (Date date : dates) {
//								if (!isPaylodeContainsWeekOff && ((new SimpleDateFormat("yyyy-MM-dd").format(date))
//										.equalsIgnoreCase(new SimpleDateFormat("yyyy-MM-dd").format(changeDate)))) {
//									existingStaffShift.setOnLeave(staffShift.getOnLeave());
//									break;
//								}
//							}
							for (java.time.LocalDate date : dates) {
								if (!isPaylodeContainsWeekOff && (date.equals(changeDate))) {
									existingStaffShift.setOnLeave(staffShift.getOnLeave());
									break;
								}
							}
							// INCREMENT TO NEXT DAY
//							changeDate = new Date(
//									changeDate.toInstant().getEpochSecond() * 1000 + SINGLE_DAY_TIMESTAMP * 1000);
							changeDate =  changeDate.plusDays(1);
						}
						existingStaffShift.setModifiedBy(createStaffShiftInput.getModifiedBy());
//						existingStaffShift.setStaffBreakTime(staffShift.getStaffBreakTime());
						List<StaffBreakTime> staffBreakTimeListForIteration = new ArrayList<>();
						if (!ObjectUtils.isEmpty(incomingStaffBreakTimeList)) {
							for (StaffBreakTime staffBreakTime: incomingStaffBreakTimeList) {
								StaffBreakTime breakTime = this.cloneBreakTime(staffBreakTime);
								staffBreakTimeListForIteration.add(breakTime);
							}
						}
						existingStaffShift.setStaffBreakTime(staffBreakTimeListForIteration);
						updatedStaffShiftList.add(existingStaffShift);
					}

					// INSERT NEW STAFF-SHIFT RECORD
					if (existingStaffShiftList.size() != createStaffShiftInput.getNoOfDays()) {

//						Date dateToBeCheck = new SimpleDateFormat("yyyy-MM-dd")
//								.parse(createStaffShiftInput.getStartDate());
						java.time.LocalDate dateToBeCheck = java.time.LocalDate.parse(createStaffShiftInput.getStartDate());

						// LOOP ON RANGE OF DAYS, CREATE NEW OBJECT IF OBJECT DOES NOT EXIST
						for (int i = 0; i < createStaffShiftInput.getNoOfDays(); i++) {
							boolean isPaylodeContainsWeekOff = false;
							final java.time.LocalDate temp = dateToBeCheck;

							long isExist = existingStaffShiftList.stream().filter(x -> x.getShiftDate().equals(temp))
									.count();

							if (isExist == 0L) {
//								String day = new SimpleDateFormat("EEEE").format(dateToBeCheck);
								String day = dateToBeCheck.format(formatterForDay);

								SStaffShifts newStaffDetail = new SStaffShifts();

								newStaffDetail.setTenantId(staffShift.getTenantId());
								newStaffDetail.setStoreId(staffShift.getStoreId());
								newStaffDetail.setStaffId(staffShift.getStaffId());

								newStaffDetail.setDay(day);
								newStaffDetail.setShiftDate(dateToBeCheck);

								if (!ObjectUtils.isEmpty(shiftSlots)
										&& !ObjectUtils.isEmpty(shiftSlots.getDayWiseShiftsTiming())) {
									String staffShiftDay = dayMap.getOrDefault(
											staffShift.getDay().substring(0, 3).toUpperCase(),
											staffShift.getDay().substring(0, 3).toUpperCase());

									String shiftDay = dayMap.getOrDefault(day.substring(0, 3).toUpperCase(),
											day.substring(0, 3).toUpperCase());

									for (DayWiseShiftsTiming dayWiseTiming : shiftSlots.getDayWiseShiftsTiming()) {
										String dayWiseTimingDay = dayMap.getOrDefault(
												dayWiseTiming.getDay().substring(0, 3).toUpperCase(),
												dayWiseTiming.getDay().substring(0, 3).toUpperCase());

										String slot = dayWiseTiming.getStartTime() + "-"
												+ dayWiseTiming.getClosureTime();

										if (staffShiftDay.equalsIgnoreCase(dayWiseTimingDay)) {
											newStaffDetail.setSlot(staffShift.getSlot().equalsIgnoreCase(slot) ? slot
													: staffShift.getSlot());
										}

										if (shiftDay.equalsIgnoreCase(dayWiseTimingDay)) {
											newStaffDetail.setSlot(slot);
										}
									}

								} else {
//									String slot = shiftSlots.getStartTime() + "-" + shiftSlots.getEndTime();
//									if (staffShift.getSlot().equalsIgnoreCase(slot)) {
//										newStaffDetail.setSlot(staffShift.getSlot());
//									}
									newStaffDetail.setSlot(staffShift.getSlot());
								}

								newStaffDetail.setEarlyOutTime(staffShift.getEarlyOutTime());

								if (personnelDetails != null && !StringUtils.isEmpty(personnelDetails.getWeeklyOff())) {
									weekOff = new ArrayList<>(Arrays.asList(personnelDetails.getWeeklyOff().split(",")));
									for (String weekDay : weekOff) {
										if (weekDay.equalsIgnoreCase(day)) {
											isPaylodeContainsWeekOff = true;
											break;
										}
									}
								}
								newStaffDetail.setWeeklyOff(isPaylodeContainsWeekOff);
//								for (Date date : dates) {
//									if (!isPaylodeContainsWeekOff
//											&& ((new SimpleDateFormat("yyyy-MM-dd").format(date)).equalsIgnoreCase(
//													new SimpleDateFormat("yyyy-MM-dd").format(dateToBeCheck)))) {
//										newStaffDetail.setOnLeave(staffShift.getOnLeave());
//										break;
//									}
//								}
								for (java.time.LocalDate date : dates) {
									if (!isPaylodeContainsWeekOff
											&& (date.equals(dateToBeCheck))) {
										newStaffDetail.setOnLeave(staffShift.getOnLeave());
										break;
									}
								}

								if (!weekOff.isEmpty() && weekOff.stream().anyMatch(l -> l.equalsIgnoreCase(day))) {
									newStaffDetail.setWeeklyOff(true);
								}

								newStaffDetail.setCreatedBy(createStaffShiftInput.getCreatedBy());
								newStaffDetail.setModifiedBy(createStaffShiftInput.getModifiedBy());
//								newStaffDetail.setStaffBreakTime(staffShift.getStaffBreakTime());
								List<StaffBreakTime> staffBreakTimeListForIteration = new ArrayList<>();
								if (!ObjectUtils.isEmpty(incomingStaffBreakTimeList)) {
									for (StaffBreakTime staffBreakTime: incomingStaffBreakTimeList) {
										StaffBreakTime breakTime = this.cloneBreakTime(staffBreakTime);
										staffBreakTimeListForIteration.add(breakTime);
									}
								}
								newStaffDetail.setStaffBreakTime(staffBreakTimeListForIteration);
								updatedStaffShiftList.add(newStaffDetail);
							}

							// INCREMENT TO NEXT DAY
//							dateToBeCheck = new Date(
//									dateToBeCheck.toInstant().getEpochSecond() * 1000 + SINGLE_DAY_TIMESTAMP * 1000);
							dateToBeCheck = dateToBeCheck.plusDays(1);
						}
					}
				}

				// BULK INSERT/UPDATE STAFF-SHIFT RECORD
				if (!updatedStaffShiftList.isEmpty()) {
					result = staffShiftsRepository.saveAll(updatedStaffShiftList);
					if (deleteBreakTimeOrphans) {
						staffBreakTimeRepo.deleteWhereStaffShiftIdIsNull();
					}
				}

				// CREATE OR UPDATE STAFF BREAK TIMES
//				createOrUpdateShifts(result, createStaffShiftInput);
				Optional<StoreDetails> storeDetailsOptional = storeDetailsRepository
						.fetchStoreAndTenantDetails(BiometricApplicationNames.RESPARK.name(), createStaffShiftInput.getTenantId(), createStaffShiftInput.getStoreId());
				if (storeDetailsOptional.isPresent()) {
					rosterSummaryService.addRosterSummaryToAttendanceSummary(updatedStaffShiftList);
				}
			} else {
				logger.error("Insufficient Input to Insert/Update Staff-Shift Details");
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
		}
		return result;

	}

	public void createOrUpdateShifts(List<SStaffShifts> result, CreateStaffShiftInput createStaffShiftInput)
			throws Exception {
		List<StaffBreakTime> updatedStaffBreakTime = new ArrayList<>();

		if (!result.isEmpty()) {
			for (SStaffShifts ss : result) {
				List<StaffBreakTime> staffBreaks = staffBreakTimeRepo.findByStaffShiftId(ss.getId());
				if (!staffBreaks.isEmpty()) {
					staffBreakTimeRepo.deleteInBatch(staffBreaks);
				}

				if (!ObjectUtils.isEmpty(ss.getStaffBreakTime())) {
					for (StaffBreakTime staffBreakTime : ss.getStaffBreakTime()) {
						StaffBreakTime sbt = createUpdatedBreakTime(ss, staffBreakTime);
						updatedStaffBreakTime.add(sbt);
					}
				}

				if (updatedStaffBreakTime.size() == createStaffShiftInput.getNoOfDays()) {
					updateForRemainingDays(result, createStaffShiftInput, updatedStaffBreakTime, ss);
				}
			}
			staffBreakTimeRepo.deleteWhereStaffShiftIdIsNull();
			staffBreakTimeRepo.saveAll(updatedStaffBreakTime);
		}
	}

	private StaffBreakTime createUpdatedBreakTime(SStaffShifts ss, StaffBreakTime staffBreakTime) {
		String workingSlot = ss.getSlot();
		String breakSlot = staffBreakTime.getSlot();

		LocalTime workStartTime = LocalTime.parse(workingSlot.split("-")[0]);
		LocalTime workEndTime = LocalTime.parse(workingSlot.split("-")[1]);

		LocalTime breakStartTime = LocalTime.parse(breakSlot.split("-")[0]);
		LocalTime breakEndTime = LocalTime.parse(breakSlot.split("-")[1]);

		long workHours = ChronoUnit.HOURS.between(workStartTime, workEndTime);
		long workMinutes = ChronoUnit.MINUTES.between(workStartTime, workEndTime) % 60;
		Long workingHours = workHours * 60 + workMinutes;

		long breakHours = ChronoUnit.HOURS.between(breakStartTime, breakEndTime);
		long breakMinutes = ChronoUnit.MINUTES.between(breakStartTime, breakEndTime) % 60;
		Long breakHour = breakHours * 60 + breakMinutes;

		StaffBreakTime sbt = new StaffBreakTime();
		sbt.setBreakHours(breakHour);
		sbt.setRemark(staffBreakTime.getRemark());
		sbt.setSlot(staffBreakTime.getSlot());
		sbt.setStaffId(staffBreakTime.getStaffId());
		sbt.setStaffShiftId(ss.getId());
		sbt.setTotalWorkingHours(workingHours);
		sbt.setType(staffBreakTime.getType());

		return sbt;
	}

	private StaffBreakTime cloneBreakTime(StaffBreakTime staffBreakTime) {
		StaffBreakTime clonedBreakTime = new StaffBreakTime();
		clonedBreakTime.setBreakHours(staffBreakTime.getBreakHours());
		clonedBreakTime.setStaffId(staffBreakTime.getStaffId());
		clonedBreakTime.setRemark(staffBreakTime.getRemark());
		clonedBreakTime.setTotalWorkingHours(staffBreakTime.getTotalWorkingHours());
		clonedBreakTime.setType(staffBreakTime.getType());
		clonedBreakTime.setSlot(staffBreakTime.getSlot());

		return clonedBreakTime;
	}

	private void calculateBreakTime(SStaffShifts ss, StaffBreakTime staffBreakTime) {
		String workingSlot = ss.getSlot();
		String breakSlot = staffBreakTime.getSlot();

		String[] workParts = workingSlot.split("-");
		long workStartMins = convertToMinutes(workParts[0]);
		long workEndMins = convertToMinutes(workParts[1]);
		long totalWorkingMinutes = workEndMins - workStartMins;
		if (totalWorkingMinutes < 0) totalWorkingMinutes += 1440;

		String[] breakParts = breakSlot.split("-");
		long breakStartMins = convertToMinutes(breakParts[0]);
		long breakEndMins = convertToMinutes(breakParts[1]);
		long totalBreakMinutes = breakEndMins - breakStartMins;
		if (totalBreakMinutes < 0) totalBreakMinutes += 1440;

		staffBreakTime.setTotalWorkingHours(totalWorkingMinutes);
		staffBreakTime.setBreakHours(totalBreakMinutes);
	}

	private long convertToMinutes(String timeStr) {
		if (timeStr == null || timeStr.isBlank()) return 0;
		if ("24:00".equals(timeStr)) return 1440;

		try {
			String[] parts = timeStr.split(":");
			long hours = Long.parseLong(parts[0]);
			long minutes = Long.parseLong(parts[1]);
			return hours * 60 + minutes;
		} catch (NumberFormatException e) {
			logger.error("Invalid time format: " + timeStr);
			return 0;
		}
	}

	private void updateForRemainingDays(List<SStaffShifts> result, CreateStaffShiftInput createStaffShiftInput,
			List<StaffBreakTime> updatedStaffBreakTime, SStaffShifts ss) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		Date dateToBeCheck = sdf.parse(createStaffShiftInput.getStartDate());
		java.time.LocalDate dateToBeCheck = java.time.LocalDate.parse(createStaffShiftInput.getStartDate());

		for (int i = 0; i < createStaffShiftInput.getNoOfDays(); i++) {
//			Date temp = dateToBeCheck;
			java.time.LocalDate temp = dateToBeCheck;
			long isExist = result.stream().filter(x -> x.getShiftDate().equals(temp)).count();

			if (isExist == 0L) {

				for (StaffBreakTime sbt : updatedStaffBreakTime) {
					StaffBreakTime newSbt = createUpdatedBreakTime(ss, sbt);
					newSbt.setStaffShiftId(ss.getId());
					updatedStaffBreakTime.add(newSbt);
				}
				result.add(ss);
			}
			// INCREMENT TO NEXT DAY
//			dateToBeCheck = new Date(dateToBeCheck.toInstant().getEpochSecond() * 1000 + SINGLE_DAY_TIMESTAMP * 1000);
			dateToBeCheck = dateToBeCheck.plusDays(1);
		}
	}

	public List<Date> getDatesBetweenTwoDates(Date startDate, Date endDate) {
		List<Date> dates = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		while (calendar.getTime().before(endDate)) {
			dates.add(calendar.getTime());
			calendar.add(Calendar.DATE, 1);
		}
		return dates;
	}

	public void postStaffProductivity(Long tenantId, Long storeId, String shiftDate, List<Map<String, Object>> result) {
		List<Long> staffIdsList = new ArrayList<>();
		List<SStaffShifts> staffShifts = new ArrayList<>();

		try {
			for (Map<String, Object> data : result) {
				Number expertIdNumber = (Number) data.get("expertId");
				if (expertIdNumber != null) {
					Long expertId = expertIdNumber.longValue();
					staffIdsList.add(expertId);
				}
			}

			staffShifts = staffShiftsRepository.getStaffShifts(tenantId, storeId, staffIdsList, shiftDate);

			Map<Long, SStaffShifts> staffShiftsMap = new HashMap<>();
			for (SStaffShifts ss : staffShifts) {
				staffShiftsMap.put(ss.getStaffId(), ss);
			}

			for (Map<String, Object> data : result) {
				Number expertIdNumber = (Number) data.get("expertId");
				if (expertIdNumber != null) {
					Long expertId = expertIdNumber.longValue();

					if (expertId != null) {
						SStaffShifts ss = staffShiftsMap.get(expertId);
						if (ss != null) {
							float productiveMinutes = ss.getProductiveMinutes();

							Number durationNumber = (Number) data.get("duration");
							if (durationNumber != null) {
								float duration = durationNumber.floatValue();
								if (duration != 0) {
									productiveMinutes += duration;
									ss.setProductiveMinutes(productiveMinutes);
								}
							}
						}
					}
				}
			}
			staffShiftsRepository.saveAll(staffShifts);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
		}
	}

	public void postStaffProductivityForUpdatedOrder(Long tenantId, Long storeId, String shiftDate,
			List<Map<String, Object>> result) {
		List<Long> staffIdsList = new ArrayList<>();
		List<SStaffShifts> staffShifts = new ArrayList<>();

		try {
			for (Map<String, Object> data : result) {
				Number expertIdNumber = (Number) data.get("expertId");
				if (expertIdNumber != null) {
					Long expertId = expertIdNumber.longValue();
					staffIdsList.add(expertId);
				}
			}

			staffShifts = staffShiftsRepository.getStaffShifts(tenantId, storeId, staffIdsList, shiftDate);

			Map<Long, SStaffShifts> staffShiftsMap = new HashMap<>();
			for (SStaffShifts ss : staffShifts) {
				staffShiftsMap.put(ss.getStaffId(), ss);
			}

			for (Map<String, Object> data : result) {
				Number expertIdNumber = (Number) data.get("expertId");
				if (expertIdNumber != null) {
					Long expertId = expertIdNumber.longValue();

					if (expertId != null) {
						SStaffShifts ss = staffShiftsMap.get(expertId);
						if (ss != null) {
							float productiveMinutes = ss.getProductiveMinutes();

							Number durationNumber = (Number) data.get("duration");
							if (durationNumber != null) {
								float duration = durationNumber.floatValue();
								if (duration != 0) {
									productiveMinutes -= duration;
									ss.setProductiveMinutes(productiveMinutes);
								}
							}
						}
					}
				}
			}
			staffShiftsRepository.saveAll(staffShifts);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
		}
	}

	public void updateStylistProductivity(List<Object[]> list) {
		List<SStaffShifts> staffShifts = new ArrayList<SStaffShifts>();
		List<String> shiftDateList = new ArrayList<String>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		try {
			if (list != null && !list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					Object[] data = list.get(i);
					String orderDay = dateFormat.format(data[0]);
					if (orderDay != null) {
						shiftDateList.add(orderDay);
					}
				}

				if (!shiftDateList.isEmpty()) {
					String maxDate = Collections.max(shiftDateList);
					String minDate = Collections.min(shiftDateList);
					staffShifts = staffShiftsRepository.getExistingStaffShiftsBetween(minDate, maxDate);
				}

				for (int i = 0; i < list.size(); i++) {
					Object[] data = list.get(i);
					String orderDay = dateFormat.format(data[0]);

					Long expertId = extractExpertId(data[1]);
					float duration = extractDuration(data[2]);

					if (staffShifts != null && !staffShifts.isEmpty()) {
						for (int j = 0; j < staffShifts.size(); j++) {
							SStaffShifts staffShift = staffShifts.get(j);
							String shiftDate = dateFormat.format(staffShift.getShiftDate());

							if (orderDay.equals(shiftDate) && expertId.equals(staffShift.getStaffId())) {
								float productiveMinutes = staffShift.getProductiveMinutes();
								staffShift.setProductiveMinutes(productiveMinutes + duration);
							}
						}
					}
				}

				if (staffShifts != null && !staffShifts.isEmpty()) {
					staffShiftsRepository.saveAll(staffShifts);
				}
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
		}
	}

	public String createStaffBookedShift(String startDate) throws ParseException {

		try {
			List<ShiftSlotDTO> slots = new ArrayList<>(); // apiHelper.getStaffBookedSlots(startDate);

			List<StaffBookedSlots> staffList = new ArrayList<>();
			if (!ObjectUtils.isEmpty(slots)) {
				for (ShiftSlotDTO slot : slots) {
					StaffBookedSlots staff = new StaffBookedSlots();

					staff.setBookedSlot(slot.getSlot());
					staff.setStaffId(slot.getExpertId());
					staff.setService(slot.getService());
					staff.setAppointmentId(slot.getId());
					staff.setCreatedOn(slot.getCreatedOn());
					staff.setModifiedOn(slot.getModifiedOn());
					staff.setCanceled(slot.isCancelled());
					String s = df.format(slot.getAppointmentDay());
					Date date = df.parse(s);
					staff.setAppointmentDate(date);
					staffList.add(staff);
				}
			}
			staffBookedRepo.saveAll(staffList);
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}

		return "success";
	}

	public List<Map<String, Object>> getAvailableStaffSlots(String startDate, long staffId, long tenantId, long storeId, String timeZone) {
		logger.info("Fetching available slots for StaffID: {}, Date: {}, TenantID: {}, StoreID: {}", staffId, startDate, tenantId, storeId);
		List<Map<String, Object>> result = new ArrayList<>();
		if (ObjectUtils.isEmpty(timeZone)) {
			timeZone = "Asia/Kolkata";
		}
		try {
			Optional<StoreProfileConfig> configOpt = storeProfileConfigRepository.findByTenantIdAndStoreId(tenantId, storeId);
			int defaultSlotTime = configOpt.map(StoreProfileConfig::getDefaultSlotTime).orElse(30);

			List<StaffBreakTime> breaks = staffBreakTimeRepo.getStaffBreakTimeByStaffIdAndDate(staffId, startDate);
			List<String> staffSlots = staffBookedRepo.findByAppointmentDateAndStaffId(startDate, staffId);
			String shift = staffShiftsRepository.getStaffShifts(staffId, new SimpleDateFormat("yyyy-MM-dd").parse(startDate), tenantId, storeId);

			if (shift == null || shift.isBlank()) {
				return result;
			}
			breaks.forEach(slot -> staffSlots.add(slot.getSlot()));

			String[] shiftTimes = shift.split("-");
			int shiftStartMins = parseToMinutes(shiftTimes[0]);
			int shiftEndMins = parseToMinutes(shiftTimes[1]);
			if (shiftEndMins <= shiftStartMins) shiftEndMins += 1440; // Cross-day shift

			Map<String, LocalTime> storeTiming = getStoreTimingForDate(startDate, tenantId, storeId);
			if (storeTiming == null) return result;

			int storeStartMins = storeTiming.get("start").getHour() * 60 + storeTiming.get("start").getMinute();
			int storeCloseMins = storeTiming.get("end").getHour() * 60 + storeTiming.get("end").getMinute();
			if (storeCloseMins <= storeStartMins) storeCloseMins += 1440; // Cross-day store hours

			int effectiveStart = Math.max(shiftStartMins, storeStartMins);
			int effectiveEnd = Math.min(shiftEndMins, storeCloseMins);

			int remainder = effectiveStart % defaultSlotTime;
			if (remainder > 0) {
				effectiveStart += (defaultSlotTime - remainder);
			}

			LocalDate businessDate = LocalDate.parse(startDate);
			LocalDate today = LocalDate.now();
			LocalTime nowTime = LocalTime.now(ZoneId.of(timeZone));
			int actualCurrentMins = nowTime.getHour() * 60 + nowTime.getMinute();

			if (businessDate.equals(today.minusDays(1)) && actualCurrentMins < (effectiveEnd % 1440)) {
				actualCurrentMins += 1440;
			} else if (businessDate.isBefore(today)) {
				actualCurrentMins = Integer.MAX_VALUE;
			} else if (businessDate.isAfter(today)) {
				actualCurrentMins = -1;
			}

			logger.info("Generating available slots from {} to {} (current effective mins: {}).", effectiveStart, effectiveEnd, actualCurrentMins);

			int safeguardCounter = 0;
			for (int currentMins = effectiveStart; currentMins <= effectiveEnd; currentMins += defaultSlotTime) {

				if (++safeguardCounter > 500) {
					logger.info("Safeguard limit reached! Exiting slot generation early for staffId: {}", staffId);
					break;
				}

				if (currentMins <= actualCurrentMins && actualCurrentMins != -1) {
					continue;
				}
				int nextSlotEndMins = currentMins + defaultSlotTime;

				String slotTimeStr = formatMinutes(currentMins);
				boolean isAvailable = true;

				for (String busySlot : staffSlots) {
					String[] busyTimes = busySlot.split("-");
					int busyStart = parseToMinutes(busyTimes[0]);
					int busyEnd = parseToMinutes(busyTimes[1]);

					if (busyStart < effectiveStart) {
						busyStart += 1440;
						busyEnd += 1440;
					} else if (busyEnd <= busyStart) {
						busyEnd += 1440;
					}

					if (currentMins < busyEnd && nextSlotEndMins > busyStart) {
						isAvailable = false;
						break;
					}
				}
				Map<String, Object> map = new HashMap<>();
				map.put("slot", slotTimeStr);
				map.put("isAvailable", isAvailable);
				result.add(map);
			}

			logger.info("Successfully generated {} available slot entries.", result.size());
		} catch (Exception e) {
			logger.error("An error occurred while getting available staff slots. Error: {}", e.getMessage(), e);
		}
		return result;
	}

	private boolean overlaps(LocalTime slotStart, LocalTime slotEnd, LocalTime busyStart, LocalTime busyEnd) {
		return slotStart.isBefore(busyEnd) && busyStart.isBefore(slotEnd);
	}

	@Transactional
	public List<ShiftSlotDTO> createOrUpdateSlot(List<ShiftSlotDTO> slots) throws ParseException {
		List<StaffBookedSlots> staffList = new ArrayList<>();
		logger.info("Adding slots in staffBookedSlot Records: {}", slots.size());
		if (!ObjectUtils.isEmpty(slots)) {
			for (ShiftSlotDTO slot : slots) {
				StaffBookedSlots staff = new StaffBookedSlots();
				staff.setBookedSlot(slot.getSlot());
				staff.setStaffId(slot.getExpertId());
				staff.setService(slot.getService());
				staff.setAppointmentId(slot.getId());
				staff.setCreatedOn(slot.getCreatedOn());
				staff.setModifiedOn(slot.getModifiedOn());
				String s = df.format(slot.getAppointmentDay());
				Date date = df.parse(s);
				staff.setAppointmentDate(date);

				staffList.add(staff);
			}
		}

		List<StaffBookedSlots> existingSlots = staffBookedRepo
				.findByAppointmentId(slots.stream().findFirst().get().getId());
		if (!ObjectUtils.isEmpty(existingSlots)) {
			existingSlots.clear();
			staffBookedRepo.deleteByAppointmentId(slots.stream().findFirst().get().getId());
			existingSlots.addAll(staffList);
			staffBookedRepo.saveAll(existingSlots);
		} else {
			staffBookedRepo.saveAll(staffList);
		}
		return slots;
	}

	public List<StaffShiftDTO> getAllStaffsSlots(String startDate, long tenantId, long storeId, String timeZone) {
		List<StaffShiftDTO> results = new ArrayList<>();
		if (ObjectUtils.isEmpty(timeZone)) {
			timeZone = "Asia/Kolkata";
		}
		try {
			logger.info("Generating slots for all active staffs on date: {} for storeId: {} and tenantId: {}", startDate, storeId, tenantId);
			List<PersonnelDetails> pdList = personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId);
			List<SStaff> staffList = pdList.stream().map(pd -> {
				SStaff s = new SStaff();
				s.setId(pd.getId());
				s.setFirstName(pd.getFirstName());
				s.setLastName(pd.getLastName());
				s.setTenantId(pd.getApplicationTenantId());
				s.setActive(pd.getActive() != null && pd.getActive() ? 1 : 0);
				if (pd.getWeeklyOff() != null) s.setWeeklyOff(pd.getWeeklyOff().split(","));
				return s;
			}).collect(Collectors.toList());
			Optional<StoreProfileConfig> configOpt = storeProfileConfigRepository.findByTenantIdAndStoreId(tenantId, storeId);
			int defaultSlotTime = configOpt.map(StoreProfileConfig::getDefaultSlotTime).orElse(30);

			staffList = staffList.stream().filter(staff -> staff.getActive() == 1).collect(Collectors.toList());
			Map<String, LocalTime> storeTiming = getStoreTimingForDate(startDate, tenantId, storeId);
			if (storeTiming == null) return results;

			int storeStartMins = storeTiming.get("start").getHour() * 60 + storeTiming.get("start").getMinute();
			int storeCloseMins = storeTiming.get("end").getHour() * 60 + storeTiming.get("end").getMinute();
			if (storeCloseMins <= storeStartMins) storeCloseMins += 1440; // Cross-day store hours

			LocalDate businessDate = LocalDate.parse(startDate);
			LocalDate today = LocalDate.now();
			LocalTime nowTime = LocalTime.now(ZoneId.of(timeZone));
			int baseActualCurrentMins = nowTime.getHour() * 60 + nowTime.getMinute();

			for (SStaff staff : staffList) {
				String shift = staffShiftsRepository.getStaffShifts(staff.getId(), new SimpleDateFormat("yyyy-MM-dd").parse(startDate), tenantId, storeId);

				if (ObjectUtils.isEmpty(shift)) {
					continue;
				}

				StaffShiftDTO staffShift = new StaffShiftDTO();
				staffShift.setId(staff.getId());
				staffShift.setFirstName(staff.getFirstName());
				staffShift.setLastName(staff.getLastName());
				List<Map<String, Object>> result = new ArrayList<>();

				List<StaffBreakTime> breaks = staffBreakTimeRepo.getStaffBreakTimeByStaffIdAndDate(staff.getId(), startDate);
				List<String> staffSlots = staffBookedRepo.findByAppointmentDateAndStaffId(startDate, staff.getId());
				breaks.forEach(slot -> staffSlots.add(slot.getSlot()));

				String[] shiftTimes = shift.split("-");
				int shiftStartMins = parseToMinutes(shiftTimes[0]);
				int shiftEndMins = parseToMinutes(shiftTimes[1]);
				if (shiftEndMins <= shiftStartMins) shiftEndMins += 1440;

				int effectiveStart = Math.max(shiftStartMins, storeStartMins);
				int effectiveEnd = Math.min(shiftEndMins, storeCloseMins);

				int remainder = effectiveStart % defaultSlotTime;
				if (remainder != 0) {
					effectiveStart += (defaultSlotTime - remainder);
				}

				int actualCurrentMins = baseActualCurrentMins;
				if (businessDate.equals(today.minusDays(1)) && actualCurrentMins < (effectiveEnd % 1440)) {
					actualCurrentMins += 1440;
				} else if (businessDate.isBefore(today)) {
					actualCurrentMins = Integer.MAX_VALUE;
				} else if (businessDate.isAfter(today)) {
					actualCurrentMins = -1;
				}

				int safeguardCounter = 0;
				for (int currentMins = effectiveStart; currentMins <= effectiveEnd; currentMins += defaultSlotTime) {

					if (++safeguardCounter > 2000) {
						logger.info("Safeguard limit reached! Exiting slot generation early for staffId: {}", staff.getId());
						break;
					}
					if (currentMins <= actualCurrentMins && actualCurrentMins != -1) {
						continue;
					}
					int nextSlotEndMins = currentMins + defaultSlotTime;

					String slotTimeStr = formatMinutes(currentMins);
					boolean isAvailable = true;
					for (String busySlot : staffSlots) {
						String[] busyTimes = busySlot.split("-");
						int busyStart = parseToMinutes(busyTimes[0]);
						int busyEnd = parseToMinutes(busyTimes[1]);

						if (busyStart < effectiveStart) {
							busyStart += 1440;
							busyEnd += 1440;
						}
						else if (busyEnd <= busyStart) {
							busyEnd += 1440;
						}

						if (currentMins < busyEnd && nextSlotEndMins > busyStart) {
							isAvailable = false;
							break;
						}
					}

					Map<String, Object> map = new HashMap<>();
					map.put("slot", slotTimeStr);
					map.put("isAvailable", isAvailable);
					result.add(map);
				}
				staffShift.setSlots(result);
				results.add(staffShift);
			}
		} catch (Exception e) {
			logger.error("Error generating slots for all staffs: " + e.getMessage(), e);
		}
		return results;
	}

	private Long extractExpertId(Object expertIdObj) {
		if (expertIdObj instanceof Integer) {
			return ((Integer) expertIdObj).longValue();
		}
		return null;
	}

	private float extractDuration(Object durationObj) {
		if (durationObj instanceof Double) {
			return ((Double) durationObj).floatValue();
		}
		return 0.0f;
	}

	public List<Object[]> getStaffShiftsForStylistRevenueReport(Date fromDate, Date toDate, long storeId,
			long tenantId) {
		List<Object[]> results = new ArrayList<>();
		try {
			results = staffShiftsRepository.findStaffShiftCounts(fromDate, toDate, tenantId, storeId);
			logger.info("Fetching staffShifts for stylist RevenueReport for tenantId: {} storeId: {} records: {}", tenantId, storeId, results.size());
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
		}
		return results;
	}

	public ResponseModel updateWeeklyOff(Map<String, Object> staff) {
		ResponseModel responseModel = new ResponseModel();
		try {
			long id = Long.parseLong(staff.get("id").toString());
			long tenantId = Long.parseLong(staff.get("tenantId").toString());
			long storeId = Long.parseLong(staff.get("storeId").toString());
			Optional<PersonnelDetails> personnelDetailsOptional = personnelDetailsRepository
					.findByIdAndApplicationName(id, BiometricApplicationNames.RESPARK.name());

			if(personnelDetailsOptional.isPresent()){
				java.time.LocalDate shiftDate = java.time.LocalDate.now();
				List<String> weeklyOffList = new ObjectMapper().convertValue(staff.get("weeklyOff"), new TypeReference<List<String>>() {});
				List<SStaffShifts> shifts = staffShiftsRepository.findByTenantIdAndStoreIdAndStaffIdAndShiftDate(tenantId, storeId, id,
						shiftDate);
				List<SStaffShifts> staffShiftsEdited = new ArrayList<>();
				PersonnelDetails personnelDetails = personnelDetailsOptional.get();
				personnelDetails.setWeeklyOff(String.join(",", weeklyOffList));
				personnelDetailsRepository.save(personnelDetails);
				if (!ObjectUtils.isEmpty(shifts)) {
					for (SStaffShifts shift : shifts) {
						if (!weeklyOffList.isEmpty()
								&& weeklyOffList.stream().filter(Objects::nonNull)
								.anyMatch(day->StringUtils.equalsIgnoreCase(day,shift.getDay()))) {
							shift.setWeeklyOff(true);
							staffShiftsEdited.add(shift);
						} else if (shift.getWeeklyOff()) {
							shift.setWeeklyOff(false);
							shift.setOnLeave(false);
							staffShiftsEdited.add(shift);
						}
					}
					if (!staffShiftsEdited.isEmpty()) {
						staffShiftsRepository.saveAll(staffShiftsEdited);
						Optional<StoreDetails> storeDetailsOptional = storeDetailsRepository
								.fetchStoreAndTenantDetails(BiometricApplicationNames.RESPARK.name(), tenantId, storeId);
						if (storeDetailsOptional.isPresent()) {
							rosterSummaryService.addRosterSummaryToAttendanceSummary(staffShiftsEdited);
						}
					}
				}
				responseModel.setData(staff);
			}
			responseModel.setMessage("SUCCESS");
			responseModel.setCode(HttpStatus.OK);
		} catch (Exception ex) {
			logger.error("Exception inside setWeeklyOff");
			throw ex;
		}
		return responseModel;
	}

	public List<Map<String, Object>> getAttendenceByTenantIdStoreIdInBetween(long tenantId, long storeId,
																			 String fromDate, String toDate) {

		List<Map<String, Object>> tmp = new ArrayList<Map<String, Object>>();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Optional<StoreDetails> storeDetailsOpt = storeDetailsRepository.fetchStoreAndTenantDetails(BiometricApplicationNames.RESPARK.name(), tenantId, storeId);
			String formatStr = storeDetailsOpt.isPresent() && storeDetailsOpt.get().getDateFormat() != null ? storeDetailsOpt.get().getDateFormat() : "dd-MM-yyyy";
			java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern(formatStr);

			String query = "select s.id as staff_id, s.phone, ss.day, DATE_FORMAT(ss.shift_date,'%d/%b/%Y') as shift_date, ss.slot, ss.on_leave, " +
					"ROUND(MOD((TIME_TO_SEC(SUBSTRING(ss.slot, 7, 5)) - TIME_TO_SEC(SUBSTRING(ss.slot, 1, 5))) / 3600 + 24, 24), 2) AS hours, ss.shift_date as shiftDate " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate";

			String query1 = "select s.id as staff_id, concat_ws(' ', s.first_name, s.last_name) as name, s.phone, s.designation, " +
					"ROUND(SUM(productive_minutes) / 60, 2) as productiveMinutes " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate group by ss.staff_id";

			String query2 = "select s.id as staff_id, round(sum(MOD((TIME_TO_SEC(SUBSTRING(ss.slot, 7, 5)) - TIME_TO_SEC(SUBSTRING(ss.slot, 1, 5))) / 3600 + 24, 24)), 2) as total_hours, MAX(ss.slot) as slot " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate " +
					"and ss.on_leave = 0 and ss.weekly_off = 0 group by ss.staff_id";

			String query3 = "select s.id as staff_id, count(ss.on_leave) as leaves " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate and ss.on_leave = 1 group by ss.staff_id";

			String query4 = "select ROUND(SUM(break_hours) / 60, 2) as breaktime, ss.staff_id " +
					"from staff_break_time sbt " +
					"inner join staff_shifts ss on sbt.staff_id = ss.staff_id and sbt.staff_shift_id = ss.id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate " +
					"and ss.on_leave = 0 and ss.weekly_off = 0 group by ss.staff_id";


			Query nativeQuery = entityManager.createNativeQuery(query);
			Query nativeQuery1 = entityManager.createNativeQuery(query1);
			Query nativeQuery2 = entityManager.createNativeQuery(query2);
			Query nativeQuery3 = entityManager.createNativeQuery(query3);
			Query nativeQuery4 = entityManager.createNativeQuery(query4);

			for (Query q : java.util.Arrays.asList(nativeQuery, nativeQuery1, nativeQuery2, nativeQuery3, nativeQuery4)) {
				q.setParameter("tenantId", tenantId);
				q.setParameter("storeId", storeId);
				q.setParameter("fromDate", fromDate);
				q.setParameter("toDate", toDate);
			}

			@SuppressWarnings("unchecked")
			List<Object[]> list = nativeQuery.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list1 = nativeQuery1.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list2 = nativeQuery2.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list3 = nativeQuery3.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list4 = nativeQuery4.getResultList();

			// GET CONFIG FOR PRODUCTIVITY OF STYLIST
			Optional<StoreProfileConfig> configOptStylist = storeProfileConfigRepository.findByTenantIdAndStoreId(tenantId, storeId);
			boolean stylistProductivity = configOptStylist.map(StoreProfileConfig::getStylistProductivity).orElse(false);

			double breakTimeTotal = 0;

			PersonnelAttendanceModel inputPersonnelAttendanceModel = new PersonnelAttendanceModel();
			inputPersonnelAttendanceModel.setTenantId(tenantId);
			inputPersonnelAttendanceModel.setStoreId(storeId);
			inputPersonnelAttendanceModel.setFromDate(java.time.LocalDate.parse(fromDate));
			inputPersonnelAttendanceModel.setToDate(java.time.LocalDate.parse(toDate));
			inputPersonnelAttendanceModel.setApplicationName(BiometricApplicationNames.RESPARK.name());

			List<PersonnelAttendanceModel> personnelAttendanceModelList = new ArrayList<>();
			if (true) {
				ResponseModel responseModel = attendanceManagementService.getPersonnelAttendanceSummary(inputPersonnelAttendanceModel);
				if (!ObjectUtils.isEmpty(responseModel)
						&& responseModel.getCode().is2xxSuccessful()
						&& !ObjectUtils.isEmpty(responseModel.getData())) {
					objectMapper.registerModule(new JavaTimeModule());
					personnelAttendanceModelList =
							objectMapper.convertValue(responseModel.getData(), new TypeReference<List<PersonnelAttendanceModel>>() {
							});
				}
			}

			if (!list4.isEmpty()) {
				for (Object[] r4 : list4) {
					breakTimeTotal += Double.valueOf(r4[0].toString());
				}
			}

			if (list != null && !list.isEmpty() && list1 != null && !list1.isEmpty() && list2 != null
					&& !list2.isEmpty() && list3 != null) {
				for (Object[] r : list1) {
					Map<String, Object> loop1 = new HashMap<String, Object>();
					List<Map<String, Object>> tmp1 = new ArrayList<Map<String, Object>>();


					PersonnelAttendanceModel personnelAttendanceModel = null;
					if (!personnelAttendanceModelList.isEmpty()) {
						Optional<PersonnelAttendanceModel> personnelAttendanceModelOptional = personnelAttendanceModelList.stream().filter(item -> StringUtils.equalsIgnoreCase(String.valueOf(r[0]), String.valueOf(item.getStaffId()))).findFirst();
						if (personnelAttendanceModelOptional.isPresent()) {
							personnelAttendanceModel = personnelAttendanceModelOptional.get();
						}
					}

					loop1.put("name", r[1]);
					loop1.put("number", r[2]);
					loop1.put("designation", r[3]);
					loop1.put("productiveTime", r[4]);

					if (!ObjectUtils.isEmpty(personnelAttendanceModel)) {
						loop1.put("totalHoursWorkedAsPerBiometric", personnelAttendanceModel.getTotalHoursWorkedForPersonnel());
						loop1.put("totalBreakTimeAsPerBiometric", personnelAttendanceModel.getTotalBreakTimeForPersonnel());
					} else {
						loop1.put("totalHoursWorkedAsPerBiometric", 0.0);
						loop1.put("totalBreakTimeAsPerBiometric", 0.0);
					}

					for (Object[] r1 : list) {
						Map<String, Object> loop2 = new HashMap<String, Object>();

						if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r1[0]))) {

							DayWiseAttendance dayWiseAttendance = null;
							if (!ObjectUtils.isEmpty(personnelAttendanceModel)) {
								List<DayWiseAttendance> dayWiseAttendanceList = personnelAttendanceModel.getDayWiseAttendanceList();
								if (!dayWiseAttendanceList.isEmpty()) {
									Optional<DayWiseAttendance> dayWiseAttendanceOptional = dayWiseAttendanceList.stream().filter(item -> java.time.LocalDate.parse(String.valueOf(r1[7])).isEqual(item.getDateOfAttendance())).findFirst();
									if (dayWiseAttendanceOptional.isPresent()) {
										dayWiseAttendance = dayWiseAttendanceOptional.get();
									}
								}
							}

							if (!ObjectUtils.isEmpty(dayWiseAttendance)) {
								loop2.put("hoursWorkedForADayAsPerBiometric", dayWiseAttendance.getTotalHoursWorkedInADay());
								loop2.put("breakTimeForADayAsPerBiometric", dayWiseAttendance.getTotalBreakTimeInADay());
								
								if (dayWiseAttendance.getIndividualPunchesList() != null && !dayWiseAttendance.getIndividualPunchesList().isEmpty()) {
									List<com.relfor.pcs.payroll.dto.IndividualPunches> punches = new ArrayList<>(dayWiseAttendance.getIndividualPunchesList());
									punches.sort(java.util.Comparator.comparing(com.relfor.pcs.payroll.dto.IndividualPunches::getPunchTime));
									loop2.put("checkIn", punches.get(0).getPunchTime().toString());
									loop2.put("checkOut", punches.get(punches.size() - 1).getPunchTime().toString());
								} else {
									loop2.put("checkIn", null);
									loop2.put("checkOut", null);
								}
							} else {
								loop2.put("hoursWorkedForADayAsPerBiometric", 0.0);
								loop2.put("breakTimeForADayAsPerBiometric", 0.0);
								loop2.put("checkIn", null);
								loop2.put("checkOut", null);
							}

							loop2.put("day", r1[2]);
							
							String formattedDate1 = r1[3] != null ? String.valueOf(r1[3]) : "";
							if (r1[7] != null) {
								try {
									formattedDate1 = java.time.LocalDate.parse(String.valueOf(r1[7])).format(dtf);
								} catch (Exception e) {
									logger.error("Failed to format date in getAttendenceByTenantIdStoreIdInBetween: {}", r1[7]);
								}
							}
							loop2.put("date", formattedDate1);
							
							loop2.put("slot", r1[4]);
							loop2.put("onLeave", r1[5]);
							if (Boolean.TRUE.equals(r1[5])) {
								loop2.put("hours", 0.0);
							} else {
								double hrs = 0.0;
								if (r1[6] != null) {
									hrs = Double.parseDouble(r1[6].toString());
								}
								loop2.put("hours", hrs);
							}
							tmp1.add(loop2);
						}
					}

					for (Object[] r2 : list2) {
						if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r2[0]))) {
							double hrs = 0.0;
							if (r2[1] != null) {
								hrs = Double.parseDouble(r2[1].toString());
							}
							loop1.put("totalHours", hrs);
						}
					}

					if (!list3.isEmpty()) {
						for (Object[] r3 : list3) {
							if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r3[0]))) {
								loop1.put("leaves", r3[1]);
							}
						}
					} else {
						loop1.put("leaves", 0);
					}

					if (!list4.isEmpty()) {
						for (Object[] r4 : list4) {
							if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r4[1]))) {
								loop1.put("breaktime", r4[0]);
							}

						}
						loop1.putIfAbsent("breaktime", 0);

					}

					for (Object[] r2 : list2) {
						if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r2[0]))) {
							loop1.put("workingtime", r2[1]);
						}
					}

					if (!list4.isEmpty()) {
						for (Object[] r4 : list4) {
							if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r4[1]))) {
								Object workingTimeObj = loop1.get("workingtime");
								double breakTimeValue = Double.parseDouble(r4[0].toString());
								double workingTime = 0;
								String workingTimeStr = String.valueOf(loop1.get("workingtime"));
								String breakTimeStr = String.valueOf(breakTimeValue);
								if (workingTimeObj != null && workingTimeObj instanceof Number && breakTimeValue > 0) {
									workingTime = ((Number) workingTimeObj).doubleValue();
									if (Double.parseDouble(
											workingTimeStr.substring(workingTimeStr.indexOf("."))) >= Double
											.parseDouble(breakTimeStr.substring(breakTimeStr.indexOf(".")))) {
										loop1.put("workingtime", workingTime - breakTimeValue);
									} else {
										double workingHrs = workingTime - breakTimeValue;
										double workingMins = Double
												.parseDouble(breakTimeStr.substring(breakTimeStr.indexOf(".")))
												- Double.parseDouble(
												workingTimeStr.substring(workingTimeStr.indexOf(".")));
										workingMins = 0.60 - workingMins;
										workingHrs = Math.floor(workingHrs) + workingMins;
										loop1.put("workingtime", workingHrs);
									}
								} else {
									loop1.put("workingtime", 0);
								}
							}
						}
						loop1.putIfAbsent("breaktime", 0);
					}
					if (loop1.containsKey("workingtime") && !ObjectUtils.isEmpty(loop1.get("workingtime"))) {
						double hrs = loop1.get("workingtime").toString().endsWith(".60")
								|| loop1.get("workingtime").toString().endsWith(".6")
								? Double.parseDouble(loop1.get("workingtime").toString().substring(0,
								loop1.get("workingtime").toString().indexOf("."))) + 1
								: Double.parseDouble(loop1.get("workingtime").toString());
						loop1.put("workingtime", hrs);
					} else {
						loop1.put("workingtime", 0);
					}
					loop1.put("shifts", tmp1);
					loop1.put("totalBreaktime", breakTimeTotal);

					double workingTime = 0;
					Object workingTimeObj = loop1.get("workingtime");

					if (workingTimeObj instanceof Number) {
						workingTime = ((Number) workingTimeObj).doubleValue();
					}

					if (loop1.containsKey("workingtime") && !ObjectUtils.isEmpty(loop1.get("workingtime"))) {
						double hrs = loop1.get("workingtime").toString().endsWith(".60")
								|| loop1.get("workingtime").toString().endsWith(".6")
								? Double.parseDouble(loop1.get("workingtime").toString().substring(0,
								loop1.get("workingtime").toString().indexOf("."))) + 1
								: Double.parseDouble(loop1.get("workingtime").toString());
						loop1.put("workingtime", hrs);
					} else {
						loop1.put("workingtime", 0);
					}

					// CALCULATE PRODUCTIVITY OF STAFF
					if (stylistProductivity) {
						double productiveTime = 0;
						double productivity = 0;

//                        productiveTime = Double.valueOf(r[4].toString());
						if (r[4] != null) {
							String input = r[4].toString().trim();

							if (input.endsWith(".")) {
								input = input.substring(0, input.length() - 1);
							}
							productiveTime = Double.valueOf(input);
						}
						if (workingTime > 0) {
							productivity = (productiveTime / workingTime) * 100;
						}
						BigDecimal roundedProductivity = new BigDecimal(productivity).setScale(2, RoundingMode.HALF_UP);
						loop1.put("productivity", roundedProductivity.doubleValue() + "%");
					} else {
						loop1.put("productivity", 0 + "%");

					}
					tmp.add(loop1);
				}
			}

		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
		return tmp;
	}

	public List<Map<String, Object>> getAttendanceSummaryByTenantIdStoreIdInBetween(long tenantId, long storeId, String fromDate, String toDate) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String query1 = "select s.id as staff_id, concat_ws(' ', s.first_name, s.last_name) as name, s.phone, s.designation, " +
					"ROUND(SUM(productive_minutes) / 60, 2) as productiveMinutes " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate group by ss.staff_id";

			String query2 = "select s.id as staff_id, round(sum(MOD((TIME_TO_SEC(SUBSTRING(ss.slot, 7, 5)) - TIME_TO_SEC(SUBSTRING(ss.slot, 1, 5))) / 3600 + 24, 24)), 2) as total_hours, MAX(ss.slot) as slot " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate " +
					"and ss.on_leave = 0 and ss.weekly_off = 0 group by ss.staff_id";

			String query3 = "select s.id as staff_id, count(ss.on_leave) as leaves " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate and ss.on_leave = 1 group by ss.staff_id";

			String query4 = "select ROUND(SUM(break_hours) / 60, 2) as breaktime, ss.staff_id " +
					"from staff_break_time sbt " +
					"inner join staff_shifts ss on sbt.staff_id = ss.staff_id and sbt.staff_shift_id = ss.id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate " +
					"and ss.on_leave = 0 and ss.weekly_off = 0 group by ss.staff_id";

			Query nativeQuery1 = entityManager.createNativeQuery(query1);
			Query nativeQuery2 = entityManager.createNativeQuery(query2);
			Query nativeQuery3 = entityManager.createNativeQuery(query3);
			Query nativeQuery4 = entityManager.createNativeQuery(query4);

			for (Query q : java.util.Arrays.asList(nativeQuery1, nativeQuery2, nativeQuery3, nativeQuery4)) {
				q.setParameter("tenantId", tenantId);
				q.setParameter("storeId", storeId);
				q.setParameter("fromDate", fromDate);
				q.setParameter("toDate", toDate);
			}

			@SuppressWarnings("unchecked")
			List<Object[]> list1 = nativeQuery1.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list2 = nativeQuery2.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list3 = nativeQuery3.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list4 = nativeQuery4.getResultList();

			PersonnelAttendanceModel inputPersonnelAttendanceModel = new PersonnelAttendanceModel();
			inputPersonnelAttendanceModel.setTenantId(tenantId);
			inputPersonnelAttendanceModel.setStoreId(storeId);
			inputPersonnelAttendanceModel.setFromDate(java.time.LocalDate.parse(fromDate));
			inputPersonnelAttendanceModel.setToDate(java.time.LocalDate.parse(toDate));
			inputPersonnelAttendanceModel.setApplicationName(BiometricApplicationNames.RESPARK.name());

			List<PersonnelAttendanceModel> personnelAttendanceModelList = new ArrayList<>();
			if (true) {
				ResponseModel responseModel = attendanceManagementService.getPersonnelAttendanceSummary(inputPersonnelAttendanceModel);
				if (!ObjectUtils.isEmpty(responseModel) && responseModel.getCode().is2xxSuccessful() && !ObjectUtils.isEmpty(responseModel.getData())) {
					objectMapper.registerModule(new JavaTimeModule());
					personnelAttendanceModelList = objectMapper.convertValue(responseModel.getData(), new TypeReference<List<PersonnelAttendanceModel>>() {});
				}
			}

			if (list1 != null && !list1.isEmpty()) {
				for (Object[] r : list1) {
					Map<String, Object> loop1 = new HashMap<String, Object>();
					
					PersonnelAttendanceModel personnelAttendanceModel = null;
					if (!personnelAttendanceModelList.isEmpty()) {
						Optional<PersonnelAttendanceModel> opt =
								personnelAttendanceModelList.stream().filter(item -> StringUtils.equalsIgnoreCase(String.valueOf(r[0]), String.valueOf(item.getStaffId()))).findFirst();
						if (opt.isPresent()) { personnelAttendanceModel = opt.get(); }
					}

					loop1.put("staff_id", r[0]);
					loop1.put("name", r[1]);
					loop1.put("number", r[2]);
					loop1.put("designation", r[3]);
					loop1.put("productiveTime", r[4]);

					if (!ObjectUtils.isEmpty(personnelAttendanceModel)) {
						loop1.put("totalHoursWorkedAsPerBiometric", personnelAttendanceModel.getTotalHoursWorkedForPersonnel());
						loop1.put("totalBreakTimeAsPerBiometric", personnelAttendanceModel.getTotalBreakTimeForPersonnel());
					} else {
						loop1.put("totalHoursWorkedAsPerBiometric", 0.0);
						loop1.put("totalBreakTimeAsPerBiometric", 0.0);
					}

					if (list2 != null) {
						for (Object[] r2 : list2) {
							if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r2[0]))) {
								double hrs = r2[1].toString().endsWith(".60")
										? Double.parseDouble(r2[1].toString().replace(".60", ".00")) + 1
										: Double.parseDouble(r2[1].toString());
								loop1.put("totalHours", hrs);
							}
						}
					}
					loop1.putIfAbsent("totalHours", 0.0);

					if (list3 != null && !list3.isEmpty()) {
						for (Object[] r3 : list3) {
							if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r3[0]))) {
								loop1.put("leaves", r3[1]);
							}
						}
					}
					loop1.putIfAbsent("leaves", 0);

					if (list4 != null && !list4.isEmpty()) {
						for (Object[] r4 : list4) {
							if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r4[1]))) {
								loop1.put("breaktime", r4[0]);
							}
						}
					}
					loop1.putIfAbsent("breaktime", 0.0);

					resultList.add(loop1);
				}
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
		}
		return resultList;
	}

	private Map<String, LocalTime> getStoreTimingForDate(String startDate, long tenantId, long storeId) {
		List<SShiftsSlots> shiftSlotsList = shiftSlotRepo.findByTenantIdAndStoreId(tenantId, storeId);
		List<DayWiseShiftsTiming> dayWiseTimingsList = new ArrayList<>();
		if (shiftSlotsList != null && !shiftSlotsList.isEmpty()) {
			SShiftsSlots activeSlot = shiftSlotsList.stream().filter(SShiftsSlots::isActive).findFirst().orElse(shiftSlotsList.get(0));
			if (activeSlot.getDayWiseShiftsTiming() != null) {
				dayWiseTimingsList.addAll(activeSlot.getDayWiseShiftsTiming());
			}
		}
		List<Map<String, Object>> dayWiseTiming = new ArrayList<>();
		for(DayWiseShiftsTiming timing : dayWiseTimingsList) {
			Map<String, Object> map = new HashMap<>();
			map.put("day", timing.getDay());
			map.put("startTime", timing.getStartTime());
			map.put("closureTime", timing.getClosureTime());
			dayWiseTiming.add(map);
		}
		LocalDate date = LocalDate.parse(startDate);
		String[] days = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
		//String dayShort = days[date.getDayOfWeek() - 1];
		String dayShort = days[date.getDayOfWeek().getValue() - 1];
		Map<String, Object> storeTiming = dayWiseTiming.stream()
				.filter(t -> t.get("day").toString().equalsIgnoreCase(dayShort))
				.findFirst()
				.orElse(null);

		if (storeTiming == null) {
			return null;
		}

		Map<String, LocalTime> result = new HashMap<>();
		result.put("start", LocalTime.parse(storeTiming.get("startTime").toString(), formatter));
		result.put("end", LocalTime.parse(storeTiming.get("closureTime").toString(), formatter));
		return result;
	}

	private int parseToMinutes(String timeStr) {
		if (timeStr == null || timeStr.isBlank()) return 0;
		if ("24:00".equals(timeStr)) return 1440;

		String[] parts = timeStr.split(":");
		return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
	}

	private String formatMinutes(int totalMins) {
		int normalized = totalMins % 1440; // Safely wraps 25:00 back to 01:00
		int h = normalized / 60;
		int m = normalized % 60;
		return String.format("%02d:%02d", h, m);
	}

	public List<Long> getAvailableStaffIds(long tenantId, long storeId, String appointmentDay) {
		List<Long> result = staffShiftsRepository.findStaffIdsByTenantStoreAndDate(tenantId, storeId, appointmentDay);
		return result;
	}

	public List<Map<String, Object>> getAttendenceByTenantIdStoreIdInBetweenDynamic(
			AttendanceQueryRequest request) {

		if (request == null) throw new IllegalArgumentException("Request body must not be null.");
		if (request.tenantId == null) throw new IllegalArgumentException("tenantId is required.");
		if (request.storeId == null) throw new IllegalArgumentException("storeId is required.");
		if (request.dateRange == null
				|| request.dateRange.startDate == null || request.dateRange.startDate.isBlank()
				|| request.dateRange.endDate == null || request.dateRange.endDate.isBlank()) {
			throw new IllegalArgumentException("dateRange.startDate and dateRange.endDate are required (yyyy-MM-dd).");
		}

		java.time.LocalDate start;
		java.time.LocalDate end;
		try {
			start = java.time.LocalDate.parse(request.dateRange.startDate.trim());
			end = java.time.LocalDate.parse(request.dateRange.endDate.trim());
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid dateRange. Expected yyyy-MM-dd. Cause: " + e.getMessage());
		}
		if (end.isBefore(start)) throw new IllegalArgumentException("dateRange.endDate must be >= startDate.");

		Set<String> allowedStaffIds = null;
		if (request.staffIds != null && !request.staffIds.isEmpty()) {
			allowedStaffIds = new HashSet<>();
			for (String s : request.staffIds) {
				if (s == null) continue;
				String t = s.trim();
				if (!t.isEmpty()) allowedStaffIds.add(t);
			}
		}

		Integer limitReq = request.limit;
		int limit = (limitReq == null || limitReq <= 0) ? 100 : Math.min(limitReq, 1000);
		int page = (request.page == null || request.page <= 1) ? 1 : request.page;
		int offset = (page - 1) * limit;

		String sortField = request.sort != null ? request.sort.field : null;
		boolean sortDesc = request.sort == null || request.sort.order == null || "desc".equalsIgnoreCase(request.sort.order);

		boolean metricsRequested = request.metrics != null && !request.metrics.isEmpty();
		Set<String> requestedMetricSet = new HashSet<>();
		if (metricsRequested) {
			for (String m : request.metrics) {
				if (m == null) continue;
				String t = m.trim();
				if (!t.isEmpty()) requestedMetricSet.add(t);
			}
		}
		boolean sortFieldWasRequested = sortField != null && requestedMetricSet.contains(sortField);

		List<Map<String, Object>> full = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			Optional<StoreDetails> storeDetailsOpt =
					storeDetailsRepository.fetchStoreAndTenantDetails(BiometricApplicationNames.RESPARK.name(), request.tenantId,
							request.storeId);
			String formatStr = storeDetailsOpt.isPresent() && storeDetailsOpt.get().getDateFormat() != null ? storeDetailsOpt.get().getDateFormat() : "dd-MM-yyyy";
			java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern(formatStr);

			java.sql.Date fromSql = java.sql.Date.valueOf(start);
			java.sql.Date toSql = java.sql.Date.valueOf(end);

			String query = "select s.id as staff_id, s.phone, ss.day, DATE_FORMAT(ss.shift_date,'%d/%b/%Y') as shift_date, ss.slot, ss.on_leave, " +
					"ROUND(MOD((TIME_TO_SEC(SUBSTRING(ss.slot, 7, 5)) - TIME_TO_SEC(SUBSTRING(ss.slot, 1, 5))) / 3600 + 24, 24), 2) AS hours, ss.shift_date as shiftDate " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate";

			String query1 = "select s.id as staff_id, concat_ws(' ', s.first_name, s.last_name) as name, s.phone, s.designation, " +
					"ROUND(SUM(productive_minutes) / 60, 2) as productiveMinutes " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate group by ss.staff_id";

			String query2 = "select s.id as staff_id, round(sum(MOD((TIME_TO_SEC(SUBSTRING(ss.slot, 7, 5)) - TIME_TO_SEC(SUBSTRING(ss.slot, 1, 5))) / 3600 + 24, 24)), 2) as total_hours, MAX(ss.slot) as slot " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate " +
					"and ss.on_leave = 0 and ss.weekly_off = 0 group by ss.staff_id";

			String query3 = "select s.id as staff_id, count(ss.on_leave) as leaves " +
					"from personnel_details s join staff_shifts ss on s.id = ss.staff_id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate and ss.on_leave = 1 group by ss.staff_id";

			String query4 = "select ROUND(SUM(break_hours) / 60, 2) as breaktime, ss.staff_id " +
					"from staff_break_time sbt " +
					"inner join staff_shifts ss on sbt.staff_id = ss.staff_id and sbt.staff_shift_id = ss.id " +
					"where ss.tenant_id = :tenantId " +
					" and ss.store_id = :storeId " +
					" and ss.shift_date between :fromDate and :toDate " +
					"and ss.on_leave = 0 and ss.weekly_off = 0 group by ss.staff_id";

			Query nativeQuery = entityManager.createNativeQuery(query);
			Query nativeQuery1 = entityManager.createNativeQuery(query1);
			Query nativeQuery2 = entityManager.createNativeQuery(query2);
			Query nativeQuery3 = entityManager.createNativeQuery(query3);
			Query nativeQuery4 = entityManager.createNativeQuery(query4);

			for (Query q : Arrays.asList(nativeQuery, nativeQuery1, nativeQuery2, nativeQuery3, nativeQuery4)) {
				q.setParameter("tenantId", request.tenantId);
				q.setParameter("storeId", request.storeId);
				q.setParameter("fromDate", fromSql);
				q.setParameter("toDate", toSql);
			}

			@SuppressWarnings("unchecked")
			List<Object[]> list = nativeQuery.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list1 = nativeQuery1.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list2 = nativeQuery2.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list3 = nativeQuery3.getResultList();
			@SuppressWarnings("unchecked")
			List<Object[]> list4 = nativeQuery4.getResultList();

			Optional<StoreProfileConfig> configOptStylist = storeProfileConfigRepository.findByTenantIdAndStoreId(request.tenantId, request.storeId);
			boolean stylistProductivity = configOptStylist.map(StoreProfileConfig::getStylistProductivity).orElse(false);
			Map<String, String> breaktimeByStaffId = new HashMap<>();
			Map<String, Double> totalBreaktimeHoursByStaffId = new HashMap<>();
			if (list4 != null && !list4.isEmpty()) {
				for (Object[] r4 : list4) {
					if (r4 == null || r4.length < 2) continue;
					String staffIdStr = String.valueOf(r4[1]);
					if (staffIdStr == null || staffIdStr.isBlank()) continue;
					String breaktimeText = r4[0] == null ? null : r4[0].toString();
					if (breaktimeText == null || breaktimeText.isBlank()) continue;
					breaktimeByStaffId.put(staffIdStr, breaktimeText);
					totalBreaktimeHoursByStaffId.put(staffIdStr, parseHourMinuteTextToHours(breaktimeText));
				}
			}

			PersonnelAttendanceModel inputPersonnelAttendanceModel = new PersonnelAttendanceModel();
			inputPersonnelAttendanceModel.setTenantId(request.tenantId);
			inputPersonnelAttendanceModel.setStoreId(request.storeId);
			inputPersonnelAttendanceModel.setFromDate(start);
			inputPersonnelAttendanceModel.setToDate(end);

			List<PersonnelAttendanceModel> personnelAttendanceModelList = new ArrayList<>();
			if (true) {
				ResponseModel responseModel = attendanceManagementService.getPersonnelAttendanceSummary(inputPersonnelAttendanceModel);
				if (!ObjectUtils.isEmpty(responseModel)
						&& responseModel.getCode().is2xxSuccessful()
						&& !ObjectUtils.isEmpty(responseModel.getData())) {
					objectMapper.registerModule(new JavaTimeModule());
					personnelAttendanceModelList =
							objectMapper.convertValue(responseModel.getData(), new TypeReference<List<PersonnelAttendanceModel>>() {
							});
				}
			}

			if (list != null && !list.isEmpty() && list1 != null && !list1.isEmpty() && list2 != null
					&& !list2.isEmpty() && list3 != null) {
				for (Object[] r : list1) {
					String staffIdStr = String.valueOf(r[0]);
					if (allowedStaffIds != null && !allowedStaffIds.contains(staffIdStr)) continue;


					Map<String, Object> loop1 = new HashMap<>();
					List<Map<String, Object>> tmp1 = new ArrayList<>();

					PersonnelAttendanceModel personnelAttendanceModel = null;
					if (!personnelAttendanceModelList.isEmpty()) {
						Optional<PersonnelAttendanceModel> personnelAttendanceModelOptional = personnelAttendanceModelList.stream()
								.filter(item -> StringUtils.equalsIgnoreCase(String.valueOf(r[0]), String.valueOf(item.getStaffId())))
								.findFirst();
						if (personnelAttendanceModelOptional.isPresent()) {
							personnelAttendanceModel = personnelAttendanceModelOptional.get();
						}
					}

					loop1.put("staffId", r[0]);
					loop1.put("name", r[1]);
					loop1.put("number", r[2]);
					loop1.put("designation", r[3]);
					loop1.put("productiveTime", r[4]);

					if (!ObjectUtils.isEmpty(personnelAttendanceModel)) {
						loop1.put("totalHoursWorkedAsPerBiometric", personnelAttendanceModel.getTotalHoursWorkedForPersonnel());
						loop1.put("totalBreakTimeAsPerBiometric", personnelAttendanceModel.getTotalBreakTimeForPersonnel());
					} else {
						loop1.put("totalHoursWorkedAsPerBiometric", 0.0);
						loop1.put("totalBreakTimeAsPerBiometric", 0.0);
					}

					for (Object[] r1 : list) {
						Map<String, Object> loop2 = new HashMap<>();
						if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r1[0]))) {

							DayWiseAttendance dayWiseAttendance = null;
							if (!ObjectUtils.isEmpty(personnelAttendanceModel)) {
								List<DayWiseAttendance> dayWiseAttendanceList = personnelAttendanceModel.getDayWiseAttendanceList();
								if (!dayWiseAttendanceList.isEmpty()) {
									Optional<DayWiseAttendance> dayWiseAttendanceOptional = dayWiseAttendanceList.stream()
											.filter(item -> java.time.LocalDate.parse(String.valueOf(r1[7])).isEqual(item.getDateOfAttendance()))
											.findFirst();
									if (dayWiseAttendanceOptional.isPresent()) {
										dayWiseAttendance = dayWiseAttendanceOptional.get();
									}
								}
							}

							if (!ObjectUtils.isEmpty(dayWiseAttendance)) {
								loop2.put("hoursWorkedForADayAsPerBiometric", dayWiseAttendance.getTotalHoursWorkedInADay());
								loop2.put("breakTimeForADayAsPerBiometric", dayWiseAttendance.getTotalBreakTimeInADay());
							} else {
								loop2.put("hoursWorkedForADayAsPerBiometric", 0.0);
								loop2.put("breakTimeForADayAsPerBiometric", 0.0);
							}

							loop2.put("day", r1[2]);
							
							String formattedDate = r1[3] != null ? String.valueOf(r1[3]) : "";
							if (r1[7] != null) {
								try {
									formattedDate = java.time.LocalDate.parse(String.valueOf(r1[7])).format(dtf);
								} catch (Exception e) {
									logger.error("Failed to format date in getAttendenceByTenantIdStoreIdInBetween: {}", r1[7]);
								}
							}
							loop2.put("date", formattedDate);
							
							loop2.put("slot", r1[4]);
							loop2.put("onLeave", r1[5]);
							if (Boolean.TRUE.equals(r1[5])) {
								loop2.put("hours", 0.0);
							} else {
								double hrs = 0.0;
								if (r1[6] != null) {
									hrs = Double.parseDouble(r1[6].toString());
								}
								loop2.put("hours", hrs);
							}
							tmp1.add(loop2);
						}
					}

					for (Object[] r2 : list2) {
						if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r2[0]))) {
							double hrs = 0.0;
							if (r2[1] != null) {
								hrs = Double.parseDouble(r2[1].toString());
							}
							loop1.put("totalHours", hrs);
						}
					}

					if (!list3.isEmpty()) {
						for (Object[] r3 : list3) {
							if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r3[0]))) {
								loop1.put("leaves", r3[1]);
							}
						}
					} else {
						loop1.put("leaves", 0);
					}

					String breaktimeText = breaktimeByStaffId.get(staffIdStr);
					if (breaktimeText != null) {
						loop1.put("breaktime", breaktimeText);
					}
					loop1.putIfAbsent("breaktime", 0);

					for (Object[] r2 : list2) {
						if (String.valueOf(r[0]).equalsIgnoreCase(String.valueOf(r2[0]))) {
							loop1.put("workingtime", r2[1]);
						}
					}

					if (breaktimeText != null) {
						Object workingTimeObj = loop1.get("workingtime");
						double breakTimeValue = Double.parseDouble(breaktimeText);
						double workingTime = 0;
						String workingTimeStr = String.valueOf(loop1.get("workingtime"));
						String breakTimeStr = String.valueOf(breakTimeValue);
						if (workingTimeObj != null && workingTimeObj instanceof Number && breakTimeValue > 0) {
							workingTime = ((Number) workingTimeObj).doubleValue();
							if (Double.parseDouble(
									workingTimeStr.substring(workingTimeStr.indexOf("."))) >= Double
									.parseDouble(breakTimeStr.substring(breakTimeStr.indexOf(".")))) {
								loop1.put("workingtime", workingTime - breakTimeValue);
							} else {
								double workingHrs = workingTime - breakTimeValue;
								double workingMins = Double
										.parseDouble(breakTimeStr.substring(breakTimeStr.indexOf(".")))
										- Double.parseDouble(
										workingTimeStr.substring(workingTimeStr.indexOf(".")));
								workingMins = 0.60 - workingMins;
								workingHrs = Math.floor(workingHrs) + workingMins;
								loop1.put("workingtime", workingHrs);
							}
						} else {
							loop1.put("workingtime", 0);
						}
					}
					loop1.putIfAbsent("breaktime", 0);

					if (loop1.containsKey("workingtime") && !ObjectUtils.isEmpty(loop1.get("workingtime"))) {
						double hrs = loop1.get("workingtime").toString().endsWith(".60")
								|| loop1.get("workingtime").toString().endsWith(".6")
								? Double.parseDouble(loop1.get("workingtime").toString().substring(0,
								loop1.get("workingtime").toString().indexOf("."))) + 1
								: Double.parseDouble(loop1.get("workingtime").toString());
						loop1.put("workingtime", hrs);
					} else {
						loop1.put("workingtime", 0);
					}
					loop1.put("shifts", tmp1);
					loop1.put("totalBreaktime", totalBreaktimeHoursByStaffId.getOrDefault(staffIdStr, 0.0));

					double workingTime = 0;
					Object workingTimeObj = loop1.get("workingtime");
					if (workingTimeObj instanceof Number) {
						workingTime = ((Number) workingTimeObj).doubleValue();
					}

					if (loop1.containsKey("workingtime") && !ObjectUtils.isEmpty(loop1.get("workingtime"))) {
						double hrs = loop1.get("workingtime").toString().endsWith(".60")
								|| loop1.get("workingtime").toString().endsWith(".6")
								? Double.parseDouble(loop1.get("workingtime").toString().substring(0,
								loop1.get("workingtime").toString().indexOf("."))) + 1
								: Double.parseDouble(loop1.get("workingtime").toString());
						loop1.put("workingtime", hrs);
					} else {
						loop1.put("workingtime", 0);
					}

					full.add(loop1);
				}
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}

		// metrics selection
		List<Map<String, Object>> out = new ArrayList<>();
		for (Map<String, Object> row : full) {
			if (row == null) continue;
			if (!metricsRequested) {
				out.add(row);
				continue;
			}
			Map<String, Object> slim = new LinkedHashMap<>();
			slim.put("staffId", row.get("staffId"));
			for (String m : request.metrics) {
				if (m == null) continue;
				String k = m.trim();
				if (k.isEmpty()) continue;
				if ("productivity".equalsIgnoreCase(k)) continue;
				if (row.containsKey(k)) slim.put(k, row.get(k));
			}
			if (sortField != null && !sortField.isBlank() && !slim.containsKey(sortField) && row.containsKey(sortField)) {
				slim.put(sortField, row.get(sortField));
			}
			out.add(slim);
		}

		// sort
		if (sortField != null && !sortField.isBlank()) {
			final String f = sortField.trim();
			out.sort((a, b) -> {
				Object av = a != null ? a.get(f) : null;
				Object bv = b != null ? b.get(f) : null;
				if (av == null && bv == null) return 0;
				if (av == null) return sortDesc ? 1 : -1;
				if (bv == null) return sortDesc ? -1 : 1;
				try {
					BigDecimal an = new BigDecimal(av.toString().replace("%", ""));
					BigDecimal bn = new BigDecimal(bv.toString().replace("%", ""));
					int cmp = an.compareTo(bn);
					return sortDesc ? -cmp : cmp;
				} catch (Exception ignore) {
					int cmp = av.toString().compareToIgnoreCase(bv.toString());
					return sortDesc ? -cmp : cmp;
				}
			});
		}

		// page
		if (offset >= out.size()) return Collections.emptyList();
		int toIndex = Math.min(out.size(), offset + limit);
		List<Map<String, Object>> slice = new ArrayList<>(out.subList(offset, toIndex));

		// remove temporary sort field if it wasn't requested explicitly
		if (metricsRequested && sortField != null && !sortField.isBlank() && !sortFieldWasRequested) {
			for (Map<String, Object> m : slice) {
				if (m != null) m.remove(sortField);
			}
		}

		return slice;
	}

	private static double parseHourMinuteTextToHours(String hourMinuteText) {
		if (hourMinuteText == null) return 0.0;
		String s = hourMinuteText.trim();
		if (s.isEmpty()) return 0.0;
		try {
			if (!s.contains(".")) return Double.parseDouble(s);
			String[] parts = s.split("\\.", 2);
			int hours = parts[0].isBlank() ? 0 : Integer.parseInt(parts[0]);
			String minsText = parts.length > 1 ? parts[1] : "0";
			if (minsText.length() > 2) minsText = minsText.substring(0, 2);
			int mins = minsText.isBlank() ? 0 : Integer.parseInt(minsText);
			if (mins < 0) mins = 0;
			if (mins > 59) mins = 59;
			return hours + (mins / 60.0);
		} catch (Exception ignore) {
			return 0.0;
		}
	}

	public List<AttendanceDetailDto> getDetailedAttendenceByStaffIdStoreIdInBetween(
			long staffId,
			long tenantId,
			long storeId,
			String fromDate,
			String toDate) {

		List<AttendanceDetailDto> resultShifts = new ArrayList<>();

		try {
			String query = "SELECT ss.day, DATE_FORMAT(ss.shift_date,'%d/%b/%Y') AS shift_date, ss.slot, ss.on_leave, " +
					"ROUND(MOD((TIME_TO_SEC(SUBSTRING(ss.slot,7,5))-TIME_TO_SEC(SUBSTRING(ss.slot,1,5)))/3600+24,24),2) AS hours, " +
					"CASE WHEN sd.is_actual_time_based_attendance=FALSE THEN dwas.total_hours_worked_inaday ELSE dwas.sum_of_actual_hours_worked_inaday END AS biometric_hours, " +
					"CASE WHEN sd.is_actual_time_based_attendance=FALSE THEN 0 ELSE dwas.total_break_time_inaday END AS biometric_break, " +
					"DATE_FORMAT(pa.first_punch,'%H:%i') AS check_in, " +
					"DATE_FORMAT(pa.last_punch,'%H:%i') AS check_out, " +
					"ss.weekly_off, dwas.is_holiday, ss.shift_date as shiftDate, sd.date_format " +
					"FROM staff_shifts ss " +
					"JOIN tenant_company_mapping tcm ON tcm.tenant_id=ss.tenant_id AND tcm.application_name='RESPARK' " +
					"JOIN store_details sd ON sd.tenant_company_mapping_id=tcm.id AND sd.store_id=ss.store_id " +
					"LEFT JOIN day_wise_attendance_summary dwas ON dwas.staff_id=ss.staff_id AND dwas.attendance_date=ss.shift_date AND dwas.application_name='RESPARK' " +
					"LEFT JOIN (SELECT staff_id, attendance_date, MIN(punch_timestamp) AS first_punch, MAX(punch_timestamp) AS last_punch " +
					"FROM personnel_attendance " +
					"WHERE staff_id=:staffId AND attendance_date BETWEEN :fromDate AND :toDate " +
					"GROUP BY staff_id, attendance_date) pa " +
					"ON pa.staff_id=ss.staff_id AND pa.attendance_date=ss.shift_date " +
					"WHERE ss.staff_id=:staffId " +
					"AND ss.tenant_id=:tenantId " +
					"AND ss.shift_date BETWEEN :fromDate AND :toDate " +
					"ORDER BY ss.shift_date";

			Query nativeQuery = entityManager.createNativeQuery(query);
			nativeQuery.setParameter("staffId", staffId);
			nativeQuery.setParameter("tenantId", tenantId);
			nativeQuery.setParameter("fromDate", fromDate);
			nativeQuery.setParameter("toDate", toDate);

			@SuppressWarnings("unchecked")
			List<Object[]> shiftsData = nativeQuery.getResultList();

			for (Object[] row : shiftsData) {
				boolean onLeave = false;
				if (row[3] != null) {
					onLeave = (row[3] instanceof Boolean) ? (Boolean) row[3] : ((Number) row[3]).intValue() > 0;
				}
				
				boolean weeklyOff = false;
				if (row[9] != null) {
					weeklyOff = (row[9] instanceof Boolean) ? (Boolean) row[9] : ((Number) row[9]).intValue() > 0;
				}
				
				boolean isHoliday = false;
				if (row[10] != null) {
					isHoliday = (row[10] instanceof Boolean) ? (Boolean) row[10] : ((Number) row[10]).intValue() > 0;
				}
				
				java.time.LocalDate shiftDateObj = null;
				if (row[11] != null) {
					shiftDateObj = java.time.LocalDate.parse(row[11].toString());
				}
				boolean isFuture = shiftDateObj != null && shiftDateObj.isAfter(java.time.LocalDate.now());

				String formattedDateStr = (String) row[1];
				if (shiftDateObj != null) {
					String formatStr = (row.length > 12 && row[12] != null) ? row[12].toString() : "dd-MM-yyyy";
					try {
						java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern(formatStr);
						formattedDateStr = shiftDateObj.format(dtf);
					} catch (Exception e) {
						logger.error("Invalid date format string from DB: {}", formatStr);
					}
				}

				String status = "Absent";
				if (isHoliday) {
					status = "Public Holiday";
				} else if (onLeave) {
					status = "Leave";
				} else if (weeklyOff) {
					status = "Weekly Off";
				} else if (isFuture) {
					status = null;
				} else if (row[7] != null) {
					if (row[8] != null && !row[7].toString().equals(row[8].toString())) {
						status = "Present";
					} else {
						status = "Absent";
					}
				} else {
					status = "Absent";
				}

				AttendanceDetailDto dto = new AttendanceDetailDto(
						(String) row[0],
						formattedDateStr,
						(String) row[2],
						onLeave,
						onLeave ? 0.0 : (row[4] == null ? 0.0 : ((Number) row[4]).doubleValue()),
						row[5] == null ? 0.0 : ((Number) row[5]).doubleValue(),
						row[6] == null ? 0.0 : ((Number) row[6]).doubleValue(),
						(String) row[7],
						(String) row[8],
						status
				);

				resultShifts.add(dto);
			}

		} catch (Exception e) {
			logger.error("Failed to fetch detailed attendance for staffId={}, tenantId={}, storeId={}, fromDate={}, toDate={}", staffId, tenantId, storeId, fromDate, toDate, e);
		}

		return resultShifts;
	}
}

