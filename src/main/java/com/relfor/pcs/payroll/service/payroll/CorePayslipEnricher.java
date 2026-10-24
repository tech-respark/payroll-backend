package com.relfor.pcs.payroll.service.payroll;

import com.relfor.pcs.payroll.dto.SalaryComponentDTO;
import com.relfor.pcs.payroll.entity.ItDeclaration;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.PersonnelPayslipHistory;
import com.relfor.pcs.payroll.entity.ReimbursementClaim;
import com.relfor.pcs.payroll.entity.StaffLoan;
import com.relfor.pcs.payroll.entity.StatutoryConfig;
import com.relfor.pcs.payroll.repository.ItDeclarationRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.PersonnelPayslipHistoryRepository;
import com.relfor.pcs.payroll.repository.ReimbursementClaimRepository;
import com.relfor.pcs.payroll.repository.StaffLoanRepository;
import com.relfor.pcs.payroll.repository.StatutoryConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CorePayslipEnricher {

    public static final String LINE_PREFIX = "Core: ";

    private final PersonnelPayslipHistoryRepository payslipHistoryRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final StatutoryConfigRepository statutoryConfigRepository;
    private final ReimbursementClaimRepository claimRepository;
    private final StaffLoanRepository loanRepository;
    private final ItDeclarationRepository declarationRepository;

    public CorePayslipEnricher(PersonnelPayslipHistoryRepository payslipHistoryRepository,
                               PersonnelDetailsRepository personnelDetailsRepository,
                               StatutoryConfigRepository statutoryConfigRepository,
                               ReimbursementClaimRepository claimRepository,
                               StaffLoanRepository loanRepository,
                               ItDeclarationRepository declarationRepository) {
        this.payslipHistoryRepository = payslipHistoryRepository;
        this.personnelDetailsRepository = personnelDetailsRepository;
        this.statutoryConfigRepository = statutoryConfigRepository;
        this.claimRepository = claimRepository;
        this.loanRepository = loanRepository;
        this.declarationRepository = declarationRepository;
    }

    @Transactional
    public PersonnelPayslipHistory apply(Long staffId, String month, Integer year, Long tenantId, Long storeId) {
        String salaryMonth = PayrollMonths.monthName(month);
        PersonnelPayslipHistory payslip = payslipHistoryRepository
                .findByStaffIdAndSalaryMonthAndSalaryYear(staffId, salaryMonth, year)
                .orElse(null);
        if (payslip == null) {
            return null;
        }
        PersonnelDetails staff = personnelDetailsRepository.findById(staffId).orElse(null);
        if (staff == null || Boolean.TRUE.equals(staff.getSkipPayroll())) {
            return payslip;
        }

        List<SalaryComponentDTO> earnings = new ArrayList<>(payslip.getEarnings() != null ? payslip.getEarnings() : List.of());
        List<SalaryComponentDTO> deductions = new ArrayList<>(payslip.getDeductions() != null ? payslip.getDeductions() : List.of());
        earnings.removeIf(this::isCoreLine);
        deductions.removeIf(this::isCoreLine);

        BigDecimal wage = sumLines(earnings);
        if (wage.signum() <= 0) {
            wage = nvl(payslip.getTotalEarning());
        }

        for (ReimbursementClaim claim : claimRepository.findByTenantIdAndStoreIdAndStaffIdAndSalaryMonthAndSalaryYearAndStatus(
                tenantId, storeId, staffId, salaryMonth, year, "APPROVED")) {
            addLine(earnings, claim.getCategory() + " reimbursement", claim.getAmount());
        }

        BigDecimal loan = recoverLoans(tenantId, storeId, staffId, salaryMonth, year);
        addLine(deductions, "Loan EMI", loan);

        statutoryDeductions(tenantId, storeId, staff, wage, salaryMonth, year)
                .forEach((name, amount) -> addLine(deductions, name, amount));

        payslip.setEarnings(earnings);
        payslip.setDeductions(deductions);
        BigDecimal totalEarning = sumLines(earnings);
        BigDecimal totalDeduction = sumLines(deductions);
        payslip.setTotalEarning(totalEarning);
        payslip.setTotalDeduction(totalDeduction);
        payslip.setSalaryAmount(totalEarning.subtract(totalDeduction));
        return payslipHistoryRepository.save(payslip);
    }

    public boolean isSkipped(PersonnelDetails staff) {
        return staff != null && Boolean.TRUE.equals(staff.getSkipPayroll());
    }

    private Map<String, BigDecimal> statutoryDeductions(Long tenantId, Long storeId, PersonnelDetails staff,
                                                        BigDecimal wage, String month, Integer year) {
        StatutoryConfig config = statutoryConfigRepository.findByTenantIdAndStoreId(tenantId, storeId)
                .orElseGet(() -> defaultStatutory(tenantId, storeId));
        Map<String, BigDecimal> deductions = new LinkedHashMap<>();
        if (enabled(config.getPfEnabled(), staff.getPfApplicable())) {
            BigDecimal base = wage.min(nvl(config.getPfWageCeiling()));
            deductions.put("PF Employee", percent(base, config.getPfEmployeeRate()));
        }
        if (enabled(config.getEsiEnabled(), staff.getEsiApplicable())
                && wage.compareTo(nvl(config.getEsiWageCeiling())) <= 0) {
            deductions.put("ESI Employee", percent(wage, config.getEsiEmployeeRate()));
        }
        if (enabled(config.getPtEnabled(), staff.getPtApplicable())) {
            deductions.put("Professional Tax", nvl(config.getPtAmount()));
        }
        if (enabled(config.getLwfEnabled(), staff.getLwfApplicable())) {
            deductions.put("LWF Employee", nvl(config.getLwfEmployeeAmount()));
        }
        if (enabled(config.getTdsEnabled(), staff.getTdsApplicable())
                && config.getTdsRate() != null && config.getTdsRate().signum() > 0) {
            BigDecimal taxable = wage.subtract(approvedDeclaration(tenantId, storeId, staff.getId(), month, year))
                    .max(BigDecimal.ZERO);
            deductions.put("TDS", percent(taxable, config.getTdsRate()));
        }
        return deductions;
    }

    private BigDecimal approvedDeclaration(Long tenantId, Long storeId, Long staffId, String month, Integer year) {
        return declarationRepository.findFirstByTenantIdAndStoreIdAndStaffIdAndFinancialYearAndStatus(
                        tenantId, storeId, staffId, PayrollMonths.financialYear(month, year), "APPROVED")
                .map(ItDeclaration::getApprovedTotal)
                .map(total -> nvl(total).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal recoverLoans(Long tenantId, Long storeId, Long staffId, String month, Integer year) {
        List<StaffLoan> loans = loanRepository.findByTenantIdAndStoreIdAndStaffIdAndStatus(tenantId, storeId, staffId, "ACTIVE");
        BigDecimal recovered = BigDecimal.ZERO;
        for (StaffLoan loan : loans) {
            if (loanNotStarted(loan, month, year)) {
                continue;
            }
            if (month.equals(loan.getLastRecoveredMonth()) && Objects.equals(year, loan.getLastRecoveredYear())) {
                recovered = recovered.add(nvl(loan.getLastRecoveredAmount()));
                continue;
            }
            BigDecimal installment = nvl(loan.getInstallmentAmount()).min(nvl(loan.getOutstandingBalance()));
            if (installment.signum() <= 0) {
                continue;
            }
            loan.setOutstandingBalance(nvl(loan.getOutstandingBalance()).subtract(installment));
            if (loan.getRemainingInstallments() != null) {
                loan.setRemainingInstallments(Math.max(0, loan.getRemainingInstallments() - 1));
            }
            if (loan.getOutstandingBalance().signum() <= 0
                    || (loan.getRemainingInstallments() != null && loan.getRemainingInstallments() == 0)) {
                loan.setStatus("CLOSED");
                loan.setOutstandingBalance(BigDecimal.ZERO);
            }
            loan.setLastRecoveredMonth(month);
            loan.setLastRecoveredYear(year);
            loan.setLastRecoveredAmount(installment);
            loanRepository.save(loan);
            recovered = recovered.add(installment);
        }
        return recovered;
    }

    private boolean loanNotStarted(StaffLoan loan, String month, Integer year) {
        if (loan.getStartYear() == null || year == null) {
            return false;
        }
        if (loan.getStartYear() > year) {
            return true;
        }
        if (loan.getStartYear() < year) {
            return false;
        }
        if (loan.getStartMonth() == null || month == null) {
            return false;
        }
        return Month.valueOf(PayrollMonths.monthName(loan.getStartMonth())).getValue()
                > Month.valueOf(PayrollMonths.monthName(month)).getValue();
    }

    private boolean enabled(Boolean storeFlag, Boolean staffFlag) {
        return Boolean.TRUE.equals(storeFlag) && (staffFlag == null || Boolean.TRUE.equals(staffFlag));
    }

    private StatutoryConfig defaultStatutory(Long tenantId, Long storeId) {
        StatutoryConfig config = new StatutoryConfig();
        config.setTenantId(tenantId);
        config.setStoreId(storeId);
        return config;
    }

    private void addLine(List<SalaryComponentDTO> lines, String name, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return;
        }
        SalaryComponentDTO line = new SalaryComponentDTO();
        line.setComponentName(LINE_PREFIX + name);
        line.setMonthlyValue(amount.setScale(2, RoundingMode.HALF_UP));
        line.setComponentType("FORMULA");
        line.setCalculatedMonthly(true);
        lines.add(line);
    }

    private boolean isCoreLine(SalaryComponentDTO line) {
        return line != null && line.getComponentName() != null && line.getComponentName().startsWith(LINE_PREFIX);
    }

    private BigDecimal sumLines(List<SalaryComponentDTO> lines) {
        return lines.stream()
                .map(SalaryComponentDTO::getMonthlyValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percent(BigDecimal base, BigDecimal rate) {
        return nvl(base).multiply(nvl(rate)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
