package com.relfor.pcs.payroll.service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.relfor.pcs.payroll.entity.SStaffShifts;
import com.relfor.pcs.payroll.entity.StaffBreakTime;
import com.relfor.pcs.payroll.repository.SStaffShiftsRepository;
import com.relfor.pcs.payroll.repository.StaffBreakTimeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Service class for managing staff break times.
 */
@Service
public class StaffBreakTimeService {
//	@Autowired
//    protected RequestContext requestContext;

	@Autowired
	RosterSummaryService rosterSummaryService;

	private final StaffBreakTimeRepository staffBreakTimeRepo;

	private final SStaffShiftsRepository stSfRepo;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public StaffBreakTimeService(StaffBreakTimeRepository staffBreakTimeRepo, SStaffShiftsRepository stSfRepo) {
		this.staffBreakTimeRepo = staffBreakTimeRepo;
		this.stSfRepo = stSfRepo;
	}

	/**
	 * Saves a new staff break time.
	 *
	 * @param sbt the staff break time to save
	 * @return the saved staff break time
	 */
	public StaffBreakTime postStaffBreakTime(StaffBreakTime sbt) {
		try {
			SStaffShifts ss = stSfRepo.getStaffShift(sbt.getStaffShiftId());

			for (StaffBreakTime staffBreak : ss.getStaffBreakTime()) {
				List<StaffBreakTime> staffBreaks = staffBreakTimeRepo.findByStaffShiftId(staffBreak.getStaffShiftId());
				if (!staffBreaks.isEmpty()) {
					staffBreakTimeRepo.deleteInBatch(staffBreaks);

				}
			}
			String workingSlot = ss.getSlot();
			String breakSlot = sbt.getSlot();

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

			sbt.setTotalWorkingHours(workingHours);
			sbt.setBreakHours(breakHour);

			sbt.setStaffShiftId(sbt.getStaffShiftId());
			StaffBreakTime staffBreakTime;
			staffBreakTime = staffBreakTimeRepo.save(sbt);
			return staffBreakTime;
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
	}

	/**
	 * Gets all staff break times for a given store and tenant.
	 *
	 * @param storeId  the store ID
	 * @param tenantId the tenant ID
	 * @return a list of staff break times
	 */
	public List<StaffBreakTime> getStaffBreakTime(Long storeId, Long tenantId) {
		List<StaffBreakTime> sbt = new ArrayList<>();
		try {
			sbt = staffBreakTimeRepo.getStaffBreakTime(storeId, tenantId, true);
			return sbt;
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
	}

	/**
	 * Gets all staff break times for a given staff.
	 *
	 * @param staffId the staff ID
	 * @return a list of staff break times
	 */
	public List<StaffBreakTime> getStaffBreakTimeSlot(Long staffId) {
		List<StaffBreakTime> sbt = new ArrayList<>();
		try {
			sbt = staffBreakTimeRepo.getStaffBreakTimeByStaffId(staffId);
			return sbt;
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
	}

	/**
	 * Gets all staff break times for a given tenant and store, and for a given
	 * date.
	 *
	 * @param tenantId the tenant ID
	 * @param storeId  the store ID
	 * @param date     the date
	 * @return a list of staff break times
	 */
	public List<StaffBreakTime> getStaffBreakTimeByTenantIdAndStoreIdAndDate(Long tenantId, Long storeId, String date) {
		List<StaffBreakTime> sbt = new ArrayList<>();
		try {
			sbt = staffBreakTimeRepo.getStaffBreakTimeByTenantIdAndStoreIdAndDate(tenantId, storeId, date);
			return sbt;
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
	}

	/**
	 * Saves a list of staff break times.
	 *
	 * @param staffBreakTime the list of staff break times to save
	 * @return the list of saved staff break times
	 */
	public List<StaffBreakTime> postStaffBreakTime(List<StaffBreakTime> staffBreakTime) {
		List<StaffBreakTime> savedStaffBreakTimes = new ArrayList<>();

		try {
			logger.info("Adding staffBreakTime records: {}", staffBreakTime.size());
			List<SStaffShifts> staffShiftsList = new ArrayList<>();
			for (StaffBreakTime staffBreak : staffBreakTime) {
				List<StaffBreakTime> staffBreaks = staffBreakTimeRepo.getStaffBreakTimeData(staffBreak.getStaffId(),
						staffBreak.getStaffShiftId());
				if (!staffBreaks.isEmpty()) {
					staffBreakTimeRepo.deleteInBatch(staffBreaks);
				}
			}

			for (StaffBreakTime sbt : staffBreakTime) {
				SStaffShifts ss = stSfRepo.getStaffShift(sbt.getStaffShiftId());
				staffShiftsList.add(ss);

				String workingSlot = ss.getSlot();
				String breakSlot = sbt.getSlot();

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

				sbt.setTotalWorkingHours(totalWorkingMinutes);
				sbt.setBreakHours(totalBreakMinutes);

				sbt.setStaffShiftId(sbt.getStaffShiftId());
				StaffBreakTime savedStaffBreakTime = staffBreakTimeRepo.save(sbt);
				savedStaffBreakTimes.add(savedStaffBreakTime);
			}

			rosterSummaryService.addStaffBreakTimeToSummary(savedStaffBreakTimes, staffShiftsList);

			return savedStaffBreakTimes;
		} catch (Exception e) {
			logger.error(e.getClass().getName(), e);
			throw e;
		}
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
}