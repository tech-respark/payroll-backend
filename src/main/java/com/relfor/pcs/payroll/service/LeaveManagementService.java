package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityNames;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityResourceTypes;
import com.relfor.pcs.payroll.dto.LeaveApplicationDTO;
import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.repository.*;
import com.relfor.pcs.payroll.util.WeeklyOffUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveManagementService {

    private final LeaveApplicationRepository applicationRepository;
    private final LeaveLedgerService ledgerService;
    private final EmployeeLeaveEnrollmentRepository enrollmentRepository;
    private final LeavePlanRuleRepository ruleRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final LeaveApplicationLogRepository leaveApplicationLogRepository;
    private final AsyncLeaveAttendanceSyncService asyncAttendanceUpdater;
    private final StoreHolidayRepository storeHolidayRepository;
    private final EmailService emailService;
    private final ActivityRecorder activityRecorder;

    private LeaveApplicationDTO toDTO(LeaveApplication app) {
        String staffName = "Unknown Staff";
        Optional<PersonnelDetails> pd = personnelDetailsRepository.findById(app.getStaffId());
        if (pd.isPresent()) {
            staffName = pd.get().getFirstName() + (pd.get().getLastName() != null ? " " + pd.get().getLastName() : "");
        }
        return new LeaveApplicationDTO(app, staffName);
    }

    @Transactional(readOnly = true)
    public List<LeaveApplicationDTO> getStaffLeaveHistory(Long staffId) {
        return applicationRepository.findByStaffIdOrderByCreatedAtDesc(staffId).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveApplicationDTO> getPendingLeaves(Long tenantId, Long storeId, Long managerId, boolean isHrAdmin) {
        List<LeaveApplication.ApplicationStatus> statuses = List.of(LeaveApplication.ApplicationStatus.PENDING, LeaveApplication.ApplicationStatus.CANCELLATION_REQUESTED);
        if (isHrAdmin) {
            return applicationRepository.findByTenantIdAndStoreIdAndStatusInOrderByCreatedAtAsc(
                    tenantId, storeId, statuses
            ).stream().map(this::toDTO).toList();
        } else {
            return applicationRepository.findByTenantIdAndStoreIdAndReportingToAndStatusInOrderByCreatedAtAsc(
                    tenantId, storeId, managerId, statuses
            ).stream().map(this::toDTO).toList();
        }
    }

    @Transactional(readOnly = true)
    public List<LeavePlanRule> getEligibleLeaveRules(Long staffId) {
        Optional<EmployeeLeaveEnrollment> enrollment = enrollmentRepository.findFirstByStaffIdOrderByEnrolledDateDesc(staffId);
        if (enrollment.isEmpty()) return Collections.emptyList();
        
        LeavePlan plan = enrollment.get().getLeavePlan();
        return ruleRepository.findAll().stream()
                .filter(r -> r.getLeavePlan().getId().equals(plan.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEligibleLeaveTypes(Long staffId) {
        List<Map<String, Object>> types = new ArrayList<>();
        for (LeavePlanRule rule : getEligibleLeaveRules(staffId)) {
            LeaveType type = rule.getLeaveType();
            if (type == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("id", type.getId());
            row.put("leaveCode", type.getLeaveCode());
            row.put("leaveName", type.getLeaveName());
            row.put("paid", type.isPaid());
            row.put("applicableGender", type.getApplicableGender());
            row.put("tenantId", type.getTenantId());
            row.put("storeId", type.getStoreId());
            types.add(row);
        }
        return types;
    }

    @Transactional
    public LeaveApplication applyForLeave(Long tenantId, Long storeId, Long staffId, LeaveType type, LocalDate startDate, LocalDate endDate, String reason, String attachmentUrl, String leaveSession) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        long overlaps = applicationRepository.countOverlappingLeaves(staffId, startDate, endDate);
        if (overlaps > 0) {
            throw new IllegalStateException("You already have a pending or approved leave during this period.");
        }

        PersonnelDetails personnel = personnelDetailsRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Personnel not found"));
        
        String weeklyOffStr = personnel.getWeeklyOff();
        long daysRequested = 0;
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (WeeklyOffUtils.isWeeklyOff(weeklyOffStr, date)) {
                continue;
            }
            daysRequested++;
        }

        if (daysRequested == 0) {
            throw new IllegalArgumentException("The entire requested period falls on your weekly off.");
        }
        
        BigDecimal requestedDuration = BigDecimal.valueOf(daysRequested);
        if (daysRequested == 1 && ("FIRST_HALF".equals(leaveSession) || "SECOND_HALF".equals(leaveSession))) {
            requestedDuration = BigDecimal.valueOf(0.5);
        }
        Optional<EmployeeLeaveEnrollment> enrollment = enrollmentRepository.findFirstByStaffIdOrderByEnrolledDateDesc(staffId);
        if (enrollment.isEmpty()) {
            throw new IllegalStateException("Employee is not enrolled in any leave plan.");
        }
        
        LeavePlan plan = enrollment.get().getLeavePlan();
        LeavePlanRule rule = ruleRepository.findAll().stream() 
                .filter(r -> r.getLeavePlan().getId().equals(plan.getId()) && r.getLeaveType().getId().equals(type.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("This leave type is not permitted under your current plan."));

        String applicableGender = rule.getLeaveType().getApplicableGender();
        if (applicableGender != null && !applicableGender.equalsIgnoreCase("ALL")) {
            if (personnel.getGender() == null || !personnel.getGender().equalsIgnoreCase(applicableGender)) {
                throw new IllegalArgumentException("This leave type is not applicable for your gender.");
            }
        }

        if (rule.getMaxConsecutiveDays() != null && daysRequested > rule.getMaxConsecutiveDays()) {
            throw new IllegalArgumentException("Cannot exceed " + rule.getMaxConsecutiveDays() + " consecutive days for this leave type.");
        }

        if (rule.getProofRequiredAfterDays() != null && daysRequested > rule.getProofRequiredAfterDays()) {
            if (attachmentUrl == null || attachmentUrl.isBlank()) {
                throw new IllegalArgumentException("A medical certificate or proof document is required for leaves longer than " + rule.getProofRequiredAfterDays() + " days.");
            }
        }

        if (!rule.isAllowNegativeBalance()) {
            BigDecimal currentBalance = ledgerService.getOrAllocateBalance(
                    tenantId, storeId, staffId, type, rule.getAnnualAllotment(), LocalDate.now(), rule.getAccrualFrequency());
            if (currentBalance.compareTo(requestedDuration) < 0) {
                throw new IllegalStateException("Insufficient leave balance. You have " + currentBalance + " days remaining.");
            }
        }

        if ("OH".equalsIgnoreCase(type.getLeaveCode())) {
            List<StoreHoliday> storeHolidays = storeHolidayRepository.findByTenantIdAndStoreIdAndHolidayDateBetween(tenantId, storeId, startDate, endDate);
            long optionalHolidayCount = storeHolidays.stream()
                .filter(sh -> Boolean.TRUE.equals(sh.getIsOptional()))
                .count();
            if (optionalHolidayCount != daysRequested) {
                throw new IllegalArgumentException("One or more dates in your request are not designated as Optional Holidays by your store.");
            }
        }

        LeaveApplication application = LeaveApplication.builder()
                .tenantId(tenantId)
                .storeId(storeId)
                .locationId(storeId)
                .staffId(staffId)
                .leaveType(type)
                .startDate(startDate)
                .endDate(endDate)
                .requestedDays(requestedDuration)
                .leaveSession(leaveSession)
                .reason(reason)
                .attachmentUrl(attachmentUrl)
                .status(LeaveApplication.ApplicationStatus.PENDING)
                .build();

        application = applicationRepository.save(application);

        logAction(application, LeaveApplicationLog.LogAction.APPLIED, staffId, reason);
        activityRecorder.record(ActivityEvent.builder()
                .action(ActivityActions.LEAVE_APPLIED)
                .tenantId(tenantId)
                .storeId(storeId)
                .resourceType(ActivityResourceTypes.LEAVE_APPLICATION)
                .resourceId(application.getId())
                .resourceLabel(staffName(personnel))
                .summary("Applied for " + leaveLabel(type) + " (" + requestedDuration.stripTrailingZeros().toPlainString()
                        + (requestedDuration.compareTo(BigDecimal.ONE) == 0 ? " day)" : " days)"))
                .context("from", startDate)
                .context("to", endDate)
                .context("leaveCode", type.getLeaveCode())
                .build());

        try {
            String applierEmail = personnel.getEmail();
            String managerEmail = null;
            if (personnel.getReportingTo() != null) {
                Optional<PersonnelDetails> manager = personnelDetailsRepository.findById(personnel.getReportingTo());
                if (manager.isPresent()) {
                    managerEmail = manager.get().getEmail();
                }
            }
            String name = personnel.getFirstName() + (personnel.getLastName() != null ? " " + personnel.getLastName() : "");
            if (applierEmail != null) {
                emailService.sendLeaveAppliedEmail(applierEmail, name, personnel.getEmployeeCode(), String.valueOf(application.getId()), LocalDate.now().toString(), type.getLeaveCode(), startDate.toString(), endDate.toString(), reason != null ? reason : "");
            }
            if (managerEmail != null) {
                emailService.sendEmail(managerEmail, "New Leave Application", "A new leave application has been submitted by " + name + ".");
            }
        } catch (Exception e) {
            // Ignored to avoid breaking transaction
        }

        return application;
    }

    @Transactional
    public void approveLeave(Long applicationId, Long managerId, String remarks) {
        LeaveApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() != LeaveApplication.ApplicationStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING applications.");
        }

        Optional<EmployeeLeaveEnrollment> enrollment = enrollmentRepository.findFirstByStaffIdOrderByEnrolledDateDesc(application.getStaffId());
        if (enrollment.isPresent()) {
            LeavePlan plan = enrollment.get().getLeavePlan();
            Optional<LeavePlanRule> ruleOpt = ruleRepository.findAll().stream()
                    .filter(r -> r.getLeavePlan().getId().equals(plan.getId()) && r.getLeaveType().getId().equals(application.getLeaveType().getId()))
                    .findFirst();
            if (ruleOpt.isPresent() && !ruleOpt.get().isAllowNegativeBalance()) {
                BigDecimal currentBalance = ledgerService.getAvailableBalance(application.getStaffId(), application.getLeaveType().getId(), LocalDate.now());
                if (currentBalance.compareTo(application.getRequestedDays()) < 0) {
                    throw new IllegalStateException("Cannot approve: Insufficient leave balance. Staff only has " + currentBalance + " days remaining.");
                }
            }
        }

        application.setStatus(LeaveApplication.ApplicationStatus.APPROVED);
        application.setManagerRemarks(remarks);
        applicationRepository.save(application);

        ledgerService.recordTransaction(
                application.getTenantId(),
                application.getStoreId(),
                application.getStaffId(),
                application.getLeaveType(),
                application.getRequestedDays().negate(), 
                LeaveTransactionLedger.TransactionType.DEBIT_LEAVE,
                application,
                application.getStartDate()
        );

        logAction(application, LeaveApplicationLog.LogAction.APPROVED, managerId, remarks);
        activityRecorder.record(leaveEvent(application, ActivityActions.LEAVE_APPROVED,
                "Approved " + leaveLabel(application.getLeaveType()) + " for " + staffName(application.getStaffId())));

        runAfterCommit(() -> asyncAttendanceUpdater.syncApprovedLeaveToAttendance(application));
        
        try {
            Optional<PersonnelDetails> personnelOpt = personnelDetailsRepository.findById(application.getStaffId());
            if (personnelOpt.isPresent() && personnelOpt.get().getEmail() != null) {
                PersonnelDetails personnel = personnelOpt.get();
                String name = personnel.getFirstName() + (personnel.getLastName() != null ? " " + personnel.getLastName() : "");
                emailService.sendLeaveApprovedEmail(personnel.getEmail(), name, personnel.getEmployeeCode(), String.valueOf(application.getId()), LocalDate.now().toString(), application.getLeaveType().getLeaveCode(), application.getStartDate().toString(), application.getEndDate().toString(), application.getReason() != null ? application.getReason() : "");
            }
        } catch (Exception e) {
            // Ignored to avoid breaking transaction
        }
    }

    @Transactional
    public void rejectLeave(Long applicationId, Long managerId, String remarks) {
        LeaveApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() != LeaveApplication.ApplicationStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING applications.");
        }

        if (remarks == null || remarks.isBlank()) {
            throw new IllegalArgumentException("Rejection remarks are mandatory.");
        }

        application.setStatus(LeaveApplication.ApplicationStatus.REJECTED);
        application.setManagerRemarks(remarks);
        applicationRepository.save(application);

        logAction(application, LeaveApplicationLog.LogAction.REJECTED, managerId, remarks);
        activityRecorder.record(leaveEvent(application, ActivityActions.LEAVE_REJECTED,
                "Rejected " + leaveLabel(application.getLeaveType()) + " for " + staffName(application.getStaffId())));
        
        try {
            Optional<PersonnelDetails> personnelOpt = personnelDetailsRepository.findById(application.getStaffId());
            if (personnelOpt.isPresent() && personnelOpt.get().getEmail() != null) {
                PersonnelDetails personnel = personnelOpt.get();
                String name = personnel.getFirstName() + (personnel.getLastName() != null ? " " + personnel.getLastName() : "");
                emailService.sendLeaveRejectedEmail(personnel.getEmail(), name, personnel.getEmployeeCode(), String.valueOf(application.getId()), LocalDate.now().toString(), application.getLeaveType().getLeaveCode(), application.getStartDate().toString(), application.getEndDate().toString(), application.getReason() != null ? application.getReason() : "");
            }
        } catch (Exception e) {
            // Ignored to avoid breaking transaction
        }
    }

    @Transactional
    public LeaveApplication.ApplicationStatus requestCancellation(Long applicationId, Long staffId, String remarks) {
        LeaveApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!application.getStaffId().equals(staffId)) {
            throw new IllegalStateException("You can only cancel your own leave applications.");
        }

        if (application.getStatus() == LeaveApplication.ApplicationStatus.CANCELLED || application.getStatus() == LeaveApplication.ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Application is already cancelled or rejected.");
        }

        if (application.getStatus() == LeaveApplication.ApplicationStatus.PENDING) {
            application.setStatus(LeaveApplication.ApplicationStatus.CANCELLED);
            applicationRepository.save(application);
            logAction(application, LeaveApplicationLog.LogAction.CANCELLED, staffId, "Self cancelled before approval: " + remarks);
            activityRecorder.record(leaveEvent(application, ActivityActions.LEAVE_CANCELLED,
                    "Cancelled " + leaveLabel(application.getLeaveType()) + " before approval"));
            return LeaveApplication.ApplicationStatus.CANCELLED;
        } else if (application.getStatus() == LeaveApplication.ApplicationStatus.APPROVED) {
            application.setStatus(LeaveApplication.ApplicationStatus.CANCELLATION_REQUESTED);
            applicationRepository.save(application);
            logAction(application, LeaveApplicationLog.LogAction.CANCELLATION_REQUESTED, staffId, remarks);
            activityRecorder.record(leaveEvent(application, ActivityActions.LEAVE_CANCEL_REQUESTED,
                    "Requested cancellation of " + leaveLabel(application.getLeaveType())));
            return LeaveApplication.ApplicationStatus.CANCELLATION_REQUESTED;
        } else {
            throw new IllegalStateException("Cannot cancel a leave in status " + application.getStatus() + ".");
        }
    }

    @Transactional
    public void approveCancellation(Long applicationId, Long managerId, String remarks) {
        LeaveApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() != LeaveApplication.ApplicationStatus.CANCELLATION_REQUESTED) {
            throw new IllegalStateException("Only CANCELLATION_REQUESTED leaves can be approved for cancellation.");
        }

        application.setStatus(LeaveApplication.ApplicationStatus.CANCELLED);
        application.setManagerRemarks(remarks);
        applicationRepository.save(application);

        ledgerService.recordTransaction(
                application.getTenantId(),
                application.getStoreId(),
                application.getStaffId(),
                application.getLeaveType(),
                application.getRequestedDays(), 
                LeaveTransactionLedger.TransactionType.ADJUSTMENT,
                application,
                LocalDate.now()
        );

        logAction(application, LeaveApplicationLog.LogAction.CANCELLED, managerId, "Cancellation Approved: " + remarks);
        activityRecorder.record(leaveEvent(application, ActivityActions.LEAVE_CANCELLED,
                "Approved cancellation of " + leaveLabel(application.getLeaveType()) + " for " + staffName(application.getStaffId())));

        runAfterCommit(() -> asyncAttendanceUpdater.revertCancelledLeaveFromAttendance(application));
    }

    @Transactional
    public void rejectCancellation(Long applicationId, Long managerId, String remarks) {
        LeaveApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() != LeaveApplication.ApplicationStatus.CANCELLATION_REQUESTED) {
            throw new IllegalStateException("Can only reject cancellation for CANCELLATION_REQUESTED applications.");
        }

        if (remarks == null || remarks.isBlank()) {
            throw new IllegalArgumentException("Rejection remarks are mandatory.");
        }

        application.setStatus(LeaveApplication.ApplicationStatus.APPROVED);
        application.setManagerRemarks(remarks);
        applicationRepository.save(application);

        logAction(application, LeaveApplicationLog.LogAction.CANCELLATION_REJECTED, managerId, "Cancellation rejected: " + remarks);
        activityRecorder.record(leaveEvent(application, ActivityActions.LEAVE_CANCEL_REJECTED,
                "Rejected cancellation of " + leaveLabel(application.getLeaveType()) + " for " + staffName(application.getStaffId())));
    }

    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private void logAction(LeaveApplication application, LeaveApplicationLog.LogAction action, Long actorId, String remarks) {
        LeaveApplicationLog log = LeaveApplicationLog.builder()
                .leaveApplication(application)
                .action(action)
                .actorId(actorId)
                .remarks(remarks)
                .build();
        leaveApplicationLogRepository.save(log);
    }

    private ActivityEvent leaveEvent(LeaveApplication application, String action, String summary) {
        return ActivityEvent.builder()
                .action(action)
                .tenantId(application.getTenantId())
                .storeId(application.getStoreId())
                .resourceType(ActivityResourceTypes.LEAVE_APPLICATION)
                .resourceId(application.getId())
                .resourceLabel(staffName(application.getStaffId()))
                .summary(summary)
                .context("from", application.getStartDate())
                .context("to", application.getEndDate())
                .context("leaveCode", application.getLeaveType() != null ? application.getLeaveType().getLeaveCode() : null)
                .build();
    }

    private String staffName(Long staffId) {
        if (staffId == null) {
            return null;
        }
        return staffName(personnelDetailsRepository.findById(staffId).orElse(null));
    }

    private String staffName(PersonnelDetails person) {
        return ActivityNames.displayName(person);
    }

    private String leaveLabel(LeaveType type) {
        if (type == null) {
            return "leave";
        }
        if (type.getLeaveName() != null && !type.getLeaveName().isBlank()) {
            return type.getLeaveName();
        }
        return type.getLeaveCode() != null ? type.getLeaveCode() : "leave";
    }
}
