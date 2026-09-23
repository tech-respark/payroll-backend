package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveAccrualSchedulerServiceTest {

    @Mock private EmployeeLeaveEnrollmentRepository enrollmentRepository;
    @Mock private LeavePlanRuleRepository ruleRepository;
    @Mock private LeaveTransactionLedgerRepository ledgerRepository;
    @Mock private LeaveLedgerService ledgerService;
    @Mock private LeaveRuleOverrideRepository overrideRepository;

    @InjectMocks
    private LeaveAccrualSchedulerService schedulerService;

    @Test
    void testProcessMonthlyAccruals() {
        // Arrange
        EmployeeLeaveEnrollment enrollment = new EmployeeLeaveEnrollment();
        enrollment.setTenantId(1L);
        enrollment.setStoreId(1L);
        enrollment.setStaffId(10L);
        LeavePlan plan = new LeavePlan();
        plan.setId(100L);
        enrollment.setLeavePlan(plan);

        LeaveType type = new LeaveType();
        type.setId(200L);

        LeavePlanRule rule = new LeavePlanRule();
        rule.setLeaveType(type);
        rule.setAnnualAllotment(BigDecimal.valueOf(12));
        rule.setAccrualFrequency(LeavePlanRule.AccrualFrequency.MONTHLY);

        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));
        when(ruleRepository.findByLeavePlan_Id(plan.getId())).thenReturn(List.of(rule));

        // Act
        schedulerService.processMonthlyAccruals();

        // Assert
        ArgumentCaptor<LeaveTransactionLedger> captor = ArgumentCaptor.forClass(LeaveTransactionLedger.class);
        verify(ledgerRepository, times(1)).save(captor.capture());
        
        LeaveTransactionLedger savedTransaction = captor.getValue();
        assertEquals(LeaveTransactionLedger.TransactionType.ACCRUAL, savedTransaction.getTransactionType());
        // 12 / 12 = 1.00
        assertEquals(BigDecimal.valueOf(1.0).setScale(2, RoundingMode.HALF_UP), savedTransaction.getTransactionValue());
        assertEquals(10L, savedTransaction.getStaffId());
    }

    @Test
    void testProcessYearEndExpiries_WithOverride() {
        // Arrange
        EmployeeLeaveEnrollment enrollment = new EmployeeLeaveEnrollment();
        enrollment.setTenantId(1L);
        enrollment.setStoreId(1L);
        enrollment.setStaffId(10L);
        LeavePlan plan = new LeavePlan();
        plan.setId(100L);
        enrollment.setLeavePlan(plan);

        LeaveType type = new LeaveType();
        type.setId(200L);

        LeavePlanRule rule = new LeavePlanRule();
        rule.setId(50L);
        rule.setLeaveType(type);
        rule.setAnnualAllotment(BigDecimal.valueOf(12));
        rule.setMaxCarryForward(5); // Global Rule says 5
        rule.setAccrualFrequency(LeavePlanRule.AccrualFrequency.NONE);

        // Setup the override for Employee 10 to be 10 days instead of 5
        LeaveRuleOverride override = new LeaveRuleOverride();
        override.setMaxCarryForwardOverride(10);
        
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));
        when(ruleRepository.findByLeavePlan_Id(plan.getId())).thenReturn(List.of(rule));
        // Employee has a current balance of 15 days
        when(ledgerService.getAvailableBalance(eq(10L), eq(200L), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(15));
        
        when(overrideRepository.findByStaffIdAndLeavePlanRuleId(10L, 50L))
                .thenReturn(Optional.of(override));

        // Act
        schedulerService.processYearEndExpiries();

        // Assert
        ArgumentCaptor<LeaveTransactionLedger> captor = ArgumentCaptor.forClass(LeaveTransactionLedger.class);
        verify(ledgerRepository, times(1)).save(captor.capture());
        
        LeaveTransactionLedger expiryTransaction = captor.getValue();
        assertEquals(LeaveTransactionLedger.TransactionType.EXPIRY, expiryTransaction.getTransactionType());
        
        // Since balance is 15, and override is 10, exactly 5 should expire (15 - 10 = 5).
        // Since it's a debit, it should be -5
        assertEquals(BigDecimal.valueOf(-5), expiryTransaction.getTransactionValue());
    }
}
