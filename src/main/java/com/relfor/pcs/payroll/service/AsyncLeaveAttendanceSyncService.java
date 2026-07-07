package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.constants.BiometricApplicationNames;
import com.relfor.pcs.payroll.entity.DayWiseAttendanceSummary;
import com.relfor.pcs.payroll.entity.LeaveApplication;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.repository.DayWiseAttendanceSummaryRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.SStaffShiftsRepository;
import com.relfor.pcs.payroll.entity.SStaffShifts;

import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AsyncLeaveAttendanceSyncService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DayWiseAttendanceSummaryRepository dayWiseAttendanceSummaryRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final SStaffShiftsRepository sStaffShiftsRepository;

    @Async
    @Transactional
    public void syncApprovedLeaveToAttendance(LeaveApplication application) {
        try {
            Long staffId = application.getStaffId();
            PersonnelDetails personnel = personnelDetailsRepository.findById(staffId)
                    .orElseThrow(() -> new IllegalArgumentException("Personnel not found for ID: " + staffId));

            Long tenantId = personnel.getApplicationTenantId(); // Defaulting to applicationTenantId
            if (tenantId == null) {
                // Try BaseEntity tenantId if there's a getter
                tenantId = personnel.getTenantCompanyMappingId(); // Or whatever field maps tenant
            }
            Long storeId = personnel.getStoreId();
            String weeklyOffStr = personnel.getWeeklyOff();

            for (LocalDate date = application.getStartDate(); !date.isAfter(application.getEndDate()); date = date.plusDays(1)) {
                if (weeklyOffStr != null && date.getDayOfWeek().name().equalsIgnoreCase(weeklyOffStr)) {
                    continue; // Skip weekly offs, as they shouldn't consume leave
                }

                Optional<DayWiseAttendanceSummary> summaryOpt = dayWiseAttendanceSummaryRepository
                        .findByTenantIdAndStoreIdAndStaffIdAndApplicationNameAndAttendanceDate(
                                tenantId, storeId, staffId, BiometricApplicationNames.RESPARK.name(), date);

                DayWiseAttendanceSummary summary;
                if (summaryOpt.isPresent()) {
                    summary = summaryOpt.get();
                } else {
                    summary = new DayWiseAttendanceSummary();
                    summary.setTenantId(tenantId);
                    summary.setStoreId(storeId);
                    summary.setStaffId(staffId);
                    summary.setApplicationName(BiometricApplicationNames.RESPARK.name());
                    summary.setAttendanceDate(date);
                    summary.setAttendanceDayOfWeek(date.getDayOfWeek().name());
                    summary.setTotalHoursWorkedInADay(BigDecimal.ZERO);
                    summary.setSumOfActualHoursWorkedInADay(BigDecimal.ZERO);
                    summary.setTotalBreakTimeInADay(BigDecimal.ZERO);
                }

                // Update flags for Leave
                summary.setIsOnLeave(true);
                summary.setIsAbsent(false);
                summary.setIsPenaltyAbsent(false);

                if ("FIRST_HALF".equals(application.getLeaveSession()) || "SECOND_HALF".equals(application.getLeaveSession())) {
                    summary.setLeaveUnits(BigDecimal.valueOf(0.5));
                } else {
                    summary.setLeaveUnits(BigDecimal.valueOf(1.0));
                }

                dayWiseAttendanceSummaryRepository.save(summary);

                List<SStaffShifts> shifts = sStaffShiftsRepository.findByTenantIdAndStoreIdAndShiftDateAndStaffId(tenantId, storeId, date, staffId);
                if (shifts != null && !shifts.isEmpty()) {
                    for (SStaffShifts shift : shifts) {
                        shift.setOnLeave(true);
                    }
                    sStaffShiftsRepository.saveAll(shifts);
                }
            }
            logger.info("Successfully synced approved leave ID {} to DayWiseAttendanceSummary", application.getId());
        } catch (Exception e) {
            logger.error("Failed to sync approved leave ID {} to attendance: {}", application.getId(), e.getMessage());
        }
    }

    @Async
    @Transactional
    public void revertCancelledLeaveFromAttendance(LeaveApplication application) {
        try {
            Long staffId = application.getStaffId();
            PersonnelDetails personnel = personnelDetailsRepository.findById(staffId)
                    .orElseThrow(() -> new IllegalArgumentException("Personnel not found for ID: " + staffId));

            Long tenantId = personnel.getApplicationTenantId();
            if (tenantId == null) {
                tenantId = personnel.getTenantCompanyMappingId();
            }
            Long storeId = personnel.getStoreId();
            String weeklyOffStr = personnel.getWeeklyOff();

            for (LocalDate date = application.getStartDate(); !date.isAfter(application.getEndDate()); date = date.plusDays(1)) {
                if (weeklyOffStr != null && date.getDayOfWeek().name().equalsIgnoreCase(weeklyOffStr)) {
                    continue;
                }

                Optional<DayWiseAttendanceSummary> summaryOpt = dayWiseAttendanceSummaryRepository
                        .findByTenantIdAndStoreIdAndStaffIdAndApplicationNameAndAttendanceDate(
                                tenantId, storeId, staffId, BiometricApplicationNames.RESPARK.name(), date);

                if (summaryOpt.isPresent()) {
                    DayWiseAttendanceSummary summary = summaryOpt.get();
                    summary.setIsOnLeave(false);

                    // Revert to absent if they didn't work at all that day and it's not a weekly off
                    if (summary.getSumOfActualHoursWorkedInADay() == null || summary.getSumOfActualHoursWorkedInADay().compareTo(BigDecimal.ZERO) == 0) {
                        summary.setIsAbsent(true);
                        summary.setIsPresent(false);
                    }

                    dayWiseAttendanceSummaryRepository.save(summary);

                    List<SStaffShifts> shifts = sStaffShiftsRepository.findByTenantIdAndStoreIdAndShiftDateAndStaffId(tenantId, storeId, date, staffId);
                    if (shifts != null && !shifts.isEmpty()) {
                        for (SStaffShifts shift : shifts) {
                            shift.setOnLeave(false);
                        }
                        sStaffShiftsRepository.saveAll(shifts);
                    }
                }
            }
            logger.info("Successfully reverted cancelled leave ID {} from DayWiseAttendanceSummary", application.getId());
        } catch (Exception e) {
            logger.error("Failed to revert cancelled leave ID {} from attendance: {}", application.getId(), e.getMessage());
        }
    }
}


