package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveManagementService {

    private final LeaveApplicationRepository applicationRepository;
    private final LeaveLedgerService ledgerService;
    private final EmployeeLeaveEnrollmentRepository enrollmentRepository;
    private final LeavePlanRuleRepository ruleRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;
    // Assume an interface/bean exists to trigger async attendance update
    // private final AsyncAttendanceSummaryCalculation asyncAttendanceUpdater;

    @Transactional
    public LeaveApplication applyForLeave(Long personnelId, LeaveType type, LocalDate startDate, LocalDate endDate, String reason, String attachmentUrl) {
        // GUARD 1: Date Logic
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        // GUARD 2: Overlap Prevention
        long overlaps = applicationRepository.countOverlappingLeaves(personnelId, startDate, endDate);
        if (overlaps > 0) {
            throw new IllegalStateException("You already have a pending or approved leave during this period.");
        }

        // GUARD 3: Weekly Off Deduction Filter
        PersonnelDetails personnel = personnelDetailsRepository.findById(personnelId)
                .orElseThrow(() -> new IllegalArgumentException("Personnel not found"));
        
        String weeklyOffStr = personnel.getWeeklyOff();
        long daysRequested = 0;
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (weeklyOffStr != null && date.getDayOfWeek().name().equalsIgnoreCase(weeklyOffStr)) {
                continue; // Skip counting the weekly off day
            }
            daysRequested++;
        }

        if (daysRequested == 0) {
            throw new IllegalArgumentException("The entire requested period falls on your weekly off.");
        }
        
        BigDecimal requestedDuration = BigDecimal.valueOf(daysRequested);

        // GUARD 4: Policy & Balance Checks
        Optional<EmployeeLeaveEnrollment> enrollment = enrollmentRepository.findByPersonnelId(personnelId);
        if (enrollment.isEmpty()) {
            throw new IllegalStateException("Employee is not enrolled in any leave plan.");
        }
        
        LeavePlan plan = enrollment.get().getLeavePlan();
        LeavePlanRule rule = ruleRepository.findAll().stream() // Simplified fetching
                .filter(r -> r.getLeavePlan().getId().equals(plan.getId()) && r.getLeaveType().getId().equals(type.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("This leave type is not permitted under your current plan."));

        // Consecutive Day Limit Check
        if (rule.getMaxConsecutiveDays() != null && daysRequested > rule.getMaxConsecutiveDays()) {
            throw new IllegalArgumentException("Cannot exceed " + rule.getMaxConsecutiveDays() + " consecutive days for this leave type.");
        }

        // Proof/Attachment Check
        if (rule.getProofRequiredAfterDays() != null && daysRequested > rule.getProofRequiredAfterDays()) {
            if (attachmentUrl == null || attachmentUrl.isBlank()) {
                throw new IllegalArgumentException("A medical certificate or proof document is required for leaves longer than " + rule.getProofRequiredAfterDays() + " days.");
            }
        }

        // Balance Check
        if (!rule.isAllowNegativeBalance()) {
            BigDecimal currentBalance = ledgerService.getAvailableBalance(personnelId, type.getId(), LocalDate.now());
            if (currentBalance.compareTo(requestedDuration) < 0) {
                throw new IllegalStateException("Insufficient leave balance. You have " + currentBalance + " days remaining.");
            }
        }

        // Create Application
        LeaveApplication application = LeaveApplication.builder()
                .personnelId(personnelId)
                .leaveType(type)
                .startDate(startDate)
                .endDate(endDate)
                .requestedDays(requestedDuration)
                .reason(reason)
                .attachmentUrl(attachmentUrl)
                .status(LeaveApplication.ApplicationStatus.PENDING)
                .build();

        return applicationRepository.save(application);
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

        // Deduct from Ledger immediately
        ledgerService.recordTransaction(
                application.getPersonnelId(),
                application.getLeaveType(),
                application.getRequestedDays().negate(), // Negative value for Debit
                LeaveTransactionLedger.TransactionType.DEBIT_LEAVE,
                application,
                application.getStartDate()
        );
        
        // Trigger async sync with DayWiseAttendanceSummary here
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
    }

    @Transactional
    public void cancelLeave(Long applicationId) {
        LeaveApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() == LeaveApplication.ApplicationStatus.CANCELLED || application.getStatus() == LeaveApplication.ApplicationStatus.REJECTED) {
            throw new IllegalStateException("Application is already cancelled or rejected.");
        }

        boolean wasApproved = application.getStatus() == LeaveApplication.ApplicationStatus.APPROVED;

        application.setStatus(LeaveApplication.ApplicationStatus.CANCELLED);
        applicationRepository.save(application);

        if (wasApproved) {
            // Refund the ledger
            ledgerService.recordTransaction(
                    application.getPersonnelId(),
                    application.getLeaveType(),
                    application.getRequestedDays(), // Positive value for Refund
                    LeaveTransactionLedger.TransactionType.ADJUSTMENT,
                    application,
                    LocalDate.now()
            );
            
            // Trigger async revert of attendance data for these dates
        }
    }
}
