package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.LeaveApplicationDTO;
import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
    public List<LeaveApplicationDTO> getPendingLeaves(Long tenantId, Long storeId) {
        return applicationRepository.findByTenantIdAndStoreIdAndStatusInOrderByCreatedAtAsc(
                tenantId, storeId,
                List.of(LeaveApplication.ApplicationStatus.PENDING, LeaveApplication.ApplicationStatus.CANCELLATION_REQUESTED)
        ).stream().map(this::toDTO).toList();
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
            if (weeklyOffStr != null && date.getDayOfWeek().name().equalsIgnoreCase(weeklyOffStr)) {
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

        if (rule.getMaxConsecutiveDays() != null && daysRequested > rule.getMaxConsecutiveDays()) {
            throw new IllegalArgumentException("Cannot exceed " + rule.getMaxConsecutiveDays() + " consecutive days for this leave type.");
        }

        if (rule.getProofRequiredAfterDays() != null && daysRequested > rule.getProofRequiredAfterDays()) {
            if (attachmentUrl == null || attachmentUrl.isBlank()) {
                throw new IllegalArgumentException("A medical certificate or proof document is required for leaves longer than " + rule.getProofRequiredAfterDays() + " days.");
            }
        }

        if (!rule.isAllowNegativeBalance()) {
            BigDecimal currentBalance = ledgerService.getAvailableBalance(staffId, type.getId(), LocalDate.now());
            if (currentBalance.compareTo(requestedDuration) < 0) {
                throw new IllegalStateException("Insufficient leave balance. You have " + currentBalance + " days remaining.");
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

        return application;
    }

    @Transactional
    public void approveLeave(Long applicationId, Long managerId, String remarks) {
        LeaveApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() != LeaveApplication.ApplicationStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING applications.");
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
        
        asyncAttendanceUpdater.syncApprovedLeaveToAttendance(application);
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
    }

    @Transactional
    public void requestCancellation(Long applicationId, Long staffId, String remarks) {
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
        } else if (application.getStatus() == LeaveApplication.ApplicationStatus.APPROVED) {
            application.setStatus(LeaveApplication.ApplicationStatus.CANCELLATION_REQUESTED);
            applicationRepository.save(application);
            logAction(application, LeaveApplicationLog.LogAction.CANCELLATION_REQUESTED, staffId, remarks);
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
        
        asyncAttendanceUpdater.revertCancelledLeaveFromAttendance(application);
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
}
