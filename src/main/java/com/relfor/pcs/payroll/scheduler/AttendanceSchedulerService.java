package com.relfor.pcs.payroll.scheduler;

import com.relfor.pcs.payroll.dto.PayrollSchedulerInfo;
import com.relfor.pcs.payroll.repository.PayrollSchedulerInfoRepo;
import com.relfor.pcs.payroll.service.AttendanceManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceSchedulerService {

	private static final Logger logger = LoggerFactory.getLogger(AttendanceSchedulerService.class);

	@Autowired
	private PayrollSchedulerInfoRepo payrollSchedulerInfoRepo;

	@Autowired
	private AttendanceManagementService attendanceManagementService;

	@Async
	@Scheduled(fixedDelayString = "${scheduler.attendance.fixedRate:120000}") // RUNS AFTER 2MIN default
	public void schedulerReminders() {
		logger.debug("Attendance scheduler service has been started");
		// Fetch everything from 1 hour ago up to 2 minutes from now to ensure we don't miss slightly delayed jobs
		Instant startTime = Instant.now().minus(1, ChronoUnit.HOURS);
		Instant endTime = Instant.now().plus(120000, ChronoUnit.MILLIS);

		sortAttendanceReminder(startTime, endTime);
		logger.debug("Attendance scheduler service has been completed");
	}

	private void sortAttendanceReminder(Instant startTime, Instant endTime) {
 		logger.info("Inside attendance scheduler sortAttendanceReminder");
		List<PayrollSchedulerInfo> attendanceEventList = payrollSchedulerInfoRepo
				.findAllByInvocationTimeAndEvent(startTime, endTime, "ATTENDANCEDATAREMINDER");
				
		if (!ObjectUtils.isEmpty(attendanceEventList)) {
			for (PayrollSchedulerInfo entity : attendanceEventList) {
				Instant invocationTime = entity.getInvocationTime();
				// Move to next day
				entity.setInvocationTime(invocationTime.plus(1, ChronoUnit.DAYS));
			}
			
			try {
				logger.info("Calling internal attendanceManagementService.retrieveScheduledAttendanceData with {} records", attendanceEventList.size());
				attendanceManagementService.retrieveScheduledAttendanceData(attendanceEventList);
				logger.info("Successfully retrieved scheduled attendance data");
			} catch (Exception e) {
				logger.error("Error retrieving attendance data in scheduler", e);
			}
			payrollSchedulerInfoRepo.saveAll(attendanceEventList);
		}
	}
}