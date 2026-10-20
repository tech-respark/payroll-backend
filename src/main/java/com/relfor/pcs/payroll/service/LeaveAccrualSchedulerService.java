package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveAccrualSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveAccrualSchedulerService.class);

    private final EmployeeLeaveEnrollmentRepository enrollmentRepository;
    private final LeavePlanRuleRepository ruleRepository;
    private final LeaveTransactionLedgerRepository ledgerRepository;
    private final LeaveLedgerService ledgerService;
    private final LeaveRuleOverrideRepository overrideRepository;
    private final SchedulerLockService schedulerLockService;

    // Run on the 1st day of every month at midnight
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void processMonthlyAccruals() {
        if (!schedulerLockService.tryAcquire("leave-accrual-monthly", java.time.Duration.ofHours(1))) {
            return;
        }
        logger.info("Starting monthly leave accrual process.");
        List<EmployeeLeaveEnrollment> enrollments = enrollmentRepository.findAll();

        for (EmployeeLeaveEnrollment enrollment : enrollments) {
            List<LeavePlanRule> rules = ruleRepository.findByLeavePlan_Id(enrollment.getLeavePlan().getId());

            for (LeavePlanRule rule : rules) {
                if (rule.getAccrualFrequency() == LeavePlanRule.AccrualFrequency.MONTHLY) {
                    LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
                    if (alreadyAccrued(enrollment.getStaffId(), rule.getLeaveType().getId(), periodStart, periodStart.plusMonths(1))) {
                        continue;
                    }
                    BigDecimal monthlyAmount = rule.getAnnualAllotment()
                            .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

                    LeaveTransactionLedger transaction = LeaveTransactionLedger.builder()
                            .tenantId(enrollment.getTenantId())
                            .storeId(enrollment.getStoreId())
                            .locationId(enrollment.getLocationId())
                            .staffId(enrollment.getStaffId())
                            .leaveType(rule.getLeaveType())
                            .transactionValue(monthlyAmount)
                            .transactionType(LeaveTransactionLedger.TransactionType.ACCRUAL)
                            .effectiveDate(LocalDate.now())
                            .build();

                    ledgerRepository.save(transaction);
                }
            }
        }
        logger.info("Monthly leave accrual process completed.");
    }

    // Run on the 1st day of Jan, Apr, Jul, Oct at midnight
    @Scheduled(cron = "0 0 0 1 1,4,7,10 ?")
    @Transactional
    public void processQuarterlyAccruals() {
        if (!schedulerLockService.tryAcquire("leave-accrual-quarterly", java.time.Duration.ofHours(1))) {
            return;
        }
        logger.info("Starting quarterly leave accrual process.");
        List<EmployeeLeaveEnrollment> enrollments = enrollmentRepository.findAll();

        for (EmployeeLeaveEnrollment enrollment : enrollments) {
            List<LeavePlanRule> rules = ruleRepository.findByLeavePlan_Id(enrollment.getLeavePlan().getId());

            for (LeavePlanRule rule : rules) {
                if (rule.getAccrualFrequency() == LeavePlanRule.AccrualFrequency.QUARTERLY) {
                    LocalDate today = LocalDate.now();
                    int quarterStartMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
                    LocalDate periodStart = LocalDate.of(today.getYear(), quarterStartMonth, 1);
                    if (alreadyAccrued(enrollment.getStaffId(), rule.getLeaveType().getId(), periodStart, periodStart.plusMonths(3))) {
                        continue;
                    }
                    BigDecimal quarterlyAmount = rule.getAnnualAllotment()
                            .divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);

                    LeaveTransactionLedger transaction = LeaveTransactionLedger.builder()
                            .tenantId(enrollment.getTenantId())
                            .storeId(enrollment.getStoreId())
                            .locationId(enrollment.getLocationId())
                            .staffId(enrollment.getStaffId())
                            .leaveType(rule.getLeaveType())
                            .transactionValue(quarterlyAmount)
                            .transactionType(LeaveTransactionLedger.TransactionType.ACCRUAL)
                            .effectiveDate(LocalDate.now())
                            .build();

                    ledgerRepository.save(transaction);
                }
            }
        }
        logger.info("Quarterly leave accrual process completed.");
    }

    // Run on Jan 1st at 12:01 AM (Assuming financial year is calendar year for simplicity)
    @Scheduled(cron = "0 1 0 1 1 ?")
    @Transactional
    public void processYearEndExpiries() {
        if (!schedulerLockService.tryAcquire("leave-accrual-year-end", java.time.Duration.ofHours(1))) {
            return;
        }
        logger.info("Starting year-end leave expiry process.");
        List<EmployeeLeaveEnrollment> enrollments = enrollmentRepository.findAll();

        for (EmployeeLeaveEnrollment enrollment : enrollments) {
            List<LeavePlanRule> rules = ruleRepository.findByLeavePlan_Id(enrollment.getLeavePlan().getId());

            for (LeavePlanRule rule : rules) {
                // Calculate balance
                BigDecimal currentBalance = ledgerService.getAvailableBalance(
                        enrollment.getStaffId(),
                        rule.getLeaveType().getId(),
                        LocalDate.now()
                );

                Integer maxCarryForward = rule.getMaxCarryForward();

                // Check for override
                Optional<LeaveRuleOverride> overrideOpt = overrideRepository.findByStaffIdAndLeavePlanRuleId(
                        enrollment.getStaffId(), rule.getId());
                if (overrideOpt.isPresent() && overrideOpt.get().getMaxCarryForwardOverride() != null) {
                    maxCarryForward = overrideOpt.get().getMaxCarryForwardOverride();
                }

                if (maxCarryForward != null) {
                    BigDecimal maxCfDecimal = BigDecimal.valueOf(maxCarryForward);
                    if (currentBalance.compareTo(maxCfDecimal) > 0) {
                        BigDecimal lapsedAmount = currentBalance.subtract(maxCfDecimal);

                        LeaveTransactionLedger expiryTransaction = LeaveTransactionLedger.builder()
                                .tenantId(enrollment.getTenantId())
                                .storeId(enrollment.getStoreId())
                                .locationId(enrollment.getLocationId())
                                .staffId(enrollment.getStaffId())
                                .leaveType(rule.getLeaveType())
                                .transactionValue(lapsedAmount.negate()) // Debit
                                .transactionType(LeaveTransactionLedger.TransactionType.EXPIRY)
                                .effectiveDate(LocalDate.now())
                                .build();

                        ledgerRepository.save(expiryTransaction);
                    }
                }
                
                // If it's an annual plan, grant the new year's bucket immediately after expiry
                if (rule.getAccrualFrequency() == LeavePlanRule.AccrualFrequency.ANNUALLY) {
                    LocalDate yearStart = LocalDate.now().withDayOfYear(1);
                    if (alreadyAccrued(enrollment.getStaffId(), rule.getLeaveType().getId(), yearStart, yearStart.plusYears(1))) {
                        continue;
                    }
                     LeaveTransactionLedger transaction = LeaveTransactionLedger.builder()
                            .tenantId(enrollment.getTenantId())
                            .storeId(enrollment.getStoreId())
                            .locationId(enrollment.getLocationId())
                            .staffId(enrollment.getStaffId())
                            .leaveType(rule.getLeaveType())
                            .transactionValue(rule.getAnnualAllotment())
                            .transactionType(LeaveTransactionLedger.TransactionType.ACCRUAL)
                            .effectiveDate(LocalDate.now())
                            .build();

                    ledgerRepository.save(transaction);
                }
            }
        }
        logger.info("Year-end leave expiry process completed.");
    }

    private boolean alreadyAccrued(Long staffId, Long leaveTypeId, LocalDate periodStart, LocalDate periodEnd) {
        return ledgerRepository.countByStaffTypeAndPeriod(
                staffId,
                leaveTypeId,
                LeaveTransactionLedger.TransactionType.ACCRUAL,
                periodStart,
                periodEnd) > 0;
    }
}
