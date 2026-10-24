package com.relfor.pcs.payroll.service.payroll;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityResourceTypes;
import com.relfor.pcs.payroll.dto.SalaryComponentDTO;
import com.relfor.pcs.payroll.entity.FullAndFinalSettlement;
import com.relfor.pcs.payroll.entity.ItDeclaration;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.PersonnelPayslipHistory;
import com.relfor.pcs.payroll.entity.ReimbursementClaim;
import com.relfor.pcs.payroll.entity.SalaryRevision;
import com.relfor.pcs.payroll.entity.StaffLoan;
import com.relfor.pcs.payroll.entity.StatutoryConfig;
import com.relfor.pcs.payroll.entity.StoreProfileConfig;
import com.relfor.pcs.payroll.repository.FullAndFinalSettlementRepository;
import com.relfor.pcs.payroll.repository.ItDeclarationRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.PersonnelPayslipHistoryRepository;
import com.relfor.pcs.payroll.repository.ReimbursementClaimRepository;
import com.relfor.pcs.payroll.repository.SalaryRevisionRepository;
import com.relfor.pcs.payroll.repository.StaffLoanRepository;
import com.relfor.pcs.payroll.repository.StatutoryConfigRepository;
import com.relfor.pcs.payroll.repository.StoreProfileConfigRepository;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PayrollOpsService {

    private final ReimbursementClaimRepository claimRepository;
    private final ItDeclarationRepository declarationRepository;
    private final StaffLoanRepository loanRepository;
    private final SalaryRevisionRepository revisionRepository;
    private final FullAndFinalSettlementRepository settlementRepository;
    private final StatutoryConfigRepository statutoryConfigRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final PersonnelPayslipHistoryRepository payslipHistoryRepository;
    private final StoreProfileConfigRepository storeProfileConfigRepository;
    private final ActivityRecorder activityRecorder;
    private final Path logoRoot;

    public PayrollOpsService(ReimbursementClaimRepository claimRepository,
                             ItDeclarationRepository declarationRepository,
                             StaffLoanRepository loanRepository,
                             SalaryRevisionRepository revisionRepository,
                             FullAndFinalSettlementRepository settlementRepository,
                             StatutoryConfigRepository statutoryConfigRepository,
                             PersonnelDetailsRepository personnelDetailsRepository,
                             PersonnelPayslipHistoryRepository payslipHistoryRepository,
                             StoreProfileConfigRepository storeProfileConfigRepository,
                             ActivityRecorder activityRecorder,
                             @Value("${payroll.uploads-dir:uploads}") String uploadsDir) {
        this.claimRepository = claimRepository;
        this.declarationRepository = declarationRepository;
        this.loanRepository = loanRepository;
        this.revisionRepository = revisionRepository;
        this.settlementRepository = settlementRepository;
        this.statutoryConfigRepository = statutoryConfigRepository;
        this.personnelDetailsRepository = personnelDetailsRepository;
        this.payslipHistoryRepository = payslipHistoryRepository;
        this.storeProfileConfigRepository = storeProfileConfigRepository;
        this.activityRecorder = activityRecorder;
        this.logoRoot = Path.of(uploadsDir, "logos");
    }

    public StatutoryConfig getStatutory(Long tenantId, Long storeId) {
        return statutoryConfigRepository.findByTenantIdAndStoreId(tenantId, storeId).orElseGet(() -> {
            StatutoryConfig config = new StatutoryConfig();
            config.setTenantId(tenantId);
            config.setStoreId(storeId);
            return config;
        });
    }

    @Transactional
    public StatutoryConfig saveStatutory(Long tenantId, Long storeId, StatutoryConfig incoming) {
        StatutoryConfig config = statutoryConfigRepository.findByTenantIdAndStoreId(tenantId, storeId)
                .orElseGet(() -> {
                    StatutoryConfig created = new StatutoryConfig();
                    created.setTenantId(tenantId);
                    created.setStoreId(storeId);
                    return created;
                });
        config.setPfEnabled(incoming.getPfEnabled());
        config.setEsiEnabled(incoming.getEsiEnabled());
        config.setPtEnabled(incoming.getPtEnabled());
        config.setTdsEnabled(incoming.getTdsEnabled());
        config.setLwfEnabled(incoming.getLwfEnabled());
        if (incoming.getPfWageCeiling() != null) config.setPfWageCeiling(incoming.getPfWageCeiling());
        if (incoming.getEsiWageCeiling() != null) config.setEsiWageCeiling(incoming.getEsiWageCeiling());
        if (incoming.getPfEmployeeRate() != null) config.setPfEmployeeRate(incoming.getPfEmployeeRate());
        if (incoming.getPfEmployerRate() != null) config.setPfEmployerRate(incoming.getPfEmployerRate());
        if (incoming.getEsiEmployeeRate() != null) config.setEsiEmployeeRate(incoming.getEsiEmployeeRate());
        if (incoming.getEsiEmployerRate() != null) config.setEsiEmployerRate(incoming.getEsiEmployerRate());
        if (incoming.getPtAmount() != null) config.setPtAmount(incoming.getPtAmount());
        if (incoming.getLwfEmployeeAmount() != null) config.setLwfEmployeeAmount(incoming.getLwfEmployeeAmount());
        if (incoming.getLwfEmployerAmount() != null) config.setLwfEmployerAmount(incoming.getLwfEmployerAmount());
        if (incoming.getTdsRate() != null) config.setTdsRate(incoming.getTdsRate());
        StatutoryConfig saved = statutoryConfigRepository.save(config);
        record(ActivityActions.PAYROLL_STATUTORY_SAVED, tenantId, storeId, ActivityResourceTypes.STORE,
                String.valueOf(storeId), "Saved statutory configuration");
        return saved;
    }

    public List<ReimbursementClaim> listClaims(Long tenantId, Long storeId, Long staffId, boolean manager) {
        if (manager && staffId == null) {
            return claimRepository.findByTenantIdAndStoreIdOrderByIdDesc(tenantId, storeId);
        }
        return claimRepository.findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(tenantId, storeId, staffId);
    }

    @Transactional
    public ReimbursementClaim createClaim(Long tenantId, Long storeId, ReimbursementClaim request) {
        Long staffId = request.getStaffId() != null ? request.getStaffId() : SecurityUtils.getStaffId();
        ReimbursementClaim claim = new ReimbursementClaim();
        claim.setTenantId(tenantId);
        claim.setStoreId(storeId);
        claim.setStaffId(staffId);
        claim.setCategory(blank(request.getCategory(), "OTHER"));
        claim.setAmount(nvl(request.getAmount()));
        claim.setSalaryMonth(PayrollMonths.monthName(request.getSalaryMonth()));
        claim.setSalaryYear(request.getSalaryYear());
        claim.setReceiptNote(request.getReceiptNote());
        claim.setRemarks(request.getRemarks());
        claim.setStatus("PENDING");
        ReimbursementClaim saved = claimRepository.save(claim);
        record(ActivityActions.PAYROLL_CLAIM_CREATED, tenantId, storeId, ActivityResourceTypes.CLAIM,
                String.valueOf(saved.getId()), "Submitted reimbursement claim");
        return saved;
    }

    @Transactional
    public ReimbursementClaim decideClaim(Long tenantId, Long storeId, Long id, boolean approved, String remarks) {
        ReimbursementClaim claim = claimRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found."));
        requireStore(claim.getTenantId(), claim.getStoreId(), tenantId, storeId);
        claim.setStatus(approved ? "APPROVED" : "REJECTED");
        claim.setRemarks(remarks);
        claim.setDecidedBy(SecurityUtils.currentUsername());
        record(approved ? ActivityActions.PAYROLL_CLAIM_APPROVED : ActivityActions.PAYROLL_CLAIM_REJECTED,
                tenantId, storeId, ActivityResourceTypes.CLAIM, String.valueOf(id),
                (approved ? "Approved" : "Rejected") + " reimbursement claim");
        return claimRepository.save(claim);
    }

    public List<ItDeclaration> listDeclarations(Long tenantId, Long storeId, Long staffId, boolean manager) {
        if (manager && staffId == null) {
            return declarationRepository.findByTenantIdAndStoreIdOrderByIdDesc(tenantId, storeId);
        }
        return declarationRepository.findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(tenantId, storeId, staffId);
    }

    @Transactional
    public ItDeclaration saveDeclaration(Long tenantId, Long storeId, ItDeclaration request) {
        Long staffId = request.getStaffId() != null ? request.getStaffId() : SecurityUtils.getStaffId();
        ItDeclaration declaration = new ItDeclaration();
        declaration.setTenantId(tenantId);
        declaration.setStoreId(storeId);
        declaration.setStaffId(staffId);
        declaration.setFinancialYear(blank(request.getFinancialYear(), currentFy()));
        declaration.setRegime(blank(request.getRegime(), "OLD"));
        declaration.setSection80c(nvl(request.getSection80c()));
        declaration.setHra(nvl(request.getHra()));
        declaration.setOtherExemption(nvl(request.getOtherExemption()));
        declaration.setDeclaredTotal(nvl(request.getSection80c()).add(nvl(request.getHra())).add(nvl(request.getOtherExemption())));
        declaration.setApprovedTotal(BigDecimal.ZERO);
        declaration.setProofNote(request.getProofNote());
        declaration.setStatus("PENDING");
        ItDeclaration saved = declarationRepository.save(declaration);
        record(ActivityActions.PAYROLL_DECLARATION_SAVED, tenantId, storeId, ActivityResourceTypes.DECLARATION,
                String.valueOf(saved.getId()), "Submitted IT declaration");
        return saved;
    }

    @Transactional
    public ItDeclaration decideDeclaration(Long tenantId, Long storeId, Long id, boolean approved, BigDecimal approvedTotal) {
        ItDeclaration declaration = declarationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Declaration not found."));
        requireStore(declaration.getTenantId(), declaration.getStoreId(), tenantId, storeId);
        declaration.setStatus(approved ? "APPROVED" : "REJECTED");
        declaration.setApprovedTotal(approved ? nvl(approvedTotal).max(declaration.getDeclaredTotal()) : BigDecimal.ZERO);
        if (approved && (approvedTotal == null || approvedTotal.signum() <= 0)) {
            declaration.setApprovedTotal(declaration.getDeclaredTotal());
        }
        declaration.setDecidedBy(SecurityUtils.currentUsername());
        record(approved ? ActivityActions.PAYROLL_DECLARATION_APPROVED : ActivityActions.PAYROLL_DECLARATION_REJECTED,
                tenantId, storeId, ActivityResourceTypes.DECLARATION, String.valueOf(id),
                (approved ? "Approved" : "Rejected") + " IT declaration");
        return declarationRepository.save(declaration);
    }

    public List<StaffLoan> listLoans(Long tenantId, Long storeId, Long staffId, boolean manager) {
        if (manager && staffId == null) {
            return loanRepository.findByTenantIdAndStoreIdOrderByIdDesc(tenantId, storeId);
        }
        return loanRepository.findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(tenantId, storeId, staffId);
    }

    @Transactional
    public StaffLoan createLoan(Long tenantId, Long storeId, StaffLoan request) {
        StaffLoan loan = new StaffLoan();
        loan.setTenantId(tenantId);
        loan.setStoreId(storeId);
        loan.setStaffId(request.getStaffId());
        loan.setLoanType(blank(request.getLoanType(), "LOAN"));
        loan.setPrincipal(nvl(request.getPrincipal()));
        loan.setInstallmentAmount(nvl(request.getInstallmentAmount()).signum() > 0
                ? request.getInstallmentAmount() : nvl(request.getPrincipal()));
        loan.setOutstandingBalance(nvl(request.getPrincipal()));
        loan.setRemainingInstallments(request.getRemainingInstallments() != null ? request.getRemainingInstallments() : 1);
        loan.setStartMonth(PayrollMonths.monthName(request.getStartMonth()));
        loan.setStartYear(request.getStartYear());
        loan.setReferenceNumber(request.getReferenceNumber());
        loan.setRemarks(request.getRemarks());
        loan.setStatus("ACTIVE");
        StaffLoan saved = loanRepository.save(loan);
        record(ActivityActions.PAYROLL_LOAN_CREATED, tenantId, storeId, ActivityResourceTypes.LOAN,
                String.valueOf(saved.getId()), "Created staff loan");
        return saved;
    }

    @Transactional
    public StaffLoan closeLoan(Long tenantId, Long storeId, Long id) {
        StaffLoan loan = loanRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Loan not found."));
        requireStore(loan.getTenantId(), loan.getStoreId(), tenantId, storeId);
        loan.setStatus("CLOSED");
        record(ActivityActions.PAYROLL_LOAN_CLOSED, tenantId, storeId, ActivityResourceTypes.LOAN,
                String.valueOf(id), "Closed staff loan");
        return loanRepository.save(loan);
    }

    public List<SalaryRevision> listRevisions(Long tenantId, Long storeId, Long staffId) {
        if (staffId == null) {
            return revisionRepository.findByTenantIdAndStoreIdOrderByIdDesc(tenantId, storeId);
        }
        return revisionRepository.findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(tenantId, storeId, staffId);
    }

    @Transactional
    public SalaryRevision createRevision(Long tenantId, Long storeId, SalaryRevision request) {
        SalaryRevision revision = new SalaryRevision();
        revision.setTenantId(tenantId);
        revision.setStoreId(storeId);
        revision.setStaffId(request.getStaffId());
        revision.setEffectiveMonth(PayrollMonths.monthName(request.getEffectiveMonth()));
        revision.setEffectiveYear(request.getEffectiveYear());
        revision.setPreviousAmount(nvl(request.getPreviousAmount()));
        revision.setNewAmount(nvl(request.getNewAmount()));
        revision.setNotes(request.getNotes());
        if (request.getStaffId() != null && request.getNewAmount() != null) {
            personnelDetailsRepository.findById(request.getStaffId()).ifPresent(staff -> {
                if (revision.getPreviousAmount().signum() == 0) {
                    revision.setPreviousAmount(nvl(staff.getSalaryAmount()));
                }
                staff.setSalaryAmount(request.getNewAmount());
                personnelDetailsRepository.save(staff);
            });
        }
        SalaryRevision saved = revisionRepository.save(revision);
        record(ActivityActions.PAYROLL_REVISION_SAVED, tenantId, storeId, ActivityResourceTypes.STAFF,
                String.valueOf(request.getStaffId()), "Recorded salary revision");
        return saved;
    }

    public List<FullAndFinalSettlement> listSettlements(Long tenantId, Long storeId) {
        return settlementRepository.findByTenantIdAndStoreIdOrderByIdDesc(tenantId, storeId);
    }

    @Transactional
    public FullAndFinalSettlement createSettlement(Long tenantId, Long storeId, Map<String, Object> body) {
        Long staffId = longVal(body.get("staffId"));
        PersonnelDetails staff = personnelDetailsRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found."));
        LocalDate lastWorking = body.get("lastWorkingDate") != null
                ? LocalDate.parse(String.valueOf(body.get("lastWorkingDate")))
                : LocalDate.now();
        BigDecimal unpaid = nvl(staff.getSalaryAmount())
                .multiply(BigDecimal.valueOf(lastWorking.getDayOfMonth()))
                .divide(BigDecimal.valueOf(lastWorking.lengthOfMonth()), 2, RoundingMode.HALF_UP);
        BigDecimal leaveEncashment = decimal(body.get("leaveEncashment"));
        BigDecimal other = decimal(body.get("otherDeductions"));
        BigDecimal loanRecovery = loanRepository.findByTenantIdAndStoreIdAndStaffIdAndStatus(tenantId, storeId, staffId, "ACTIVE")
                .stream().map(loan -> nvl(loan.getOutstandingBalance())).reduce(BigDecimal.ZERO, BigDecimal::add);
        FullAndFinalSettlement fnf = new FullAndFinalSettlement();
        fnf.setTenantId(tenantId);
        fnf.setStoreId(storeId);
        fnf.setStaffId(staffId);
        fnf.setLastWorkingDate(lastWorking);
        fnf.setUnpaidSalary(unpaid);
        fnf.setLeaveEncashment(leaveEncashment);
        fnf.setAdvanceRecovery(loanRecovery);
        fnf.setOtherDeductions(other);
        fnf.setNetPayable(unpaid.add(leaveEncashment).subtract(loanRecovery).subtract(other));
        fnf.setRemarks(body.get("remarks") != null ? String.valueOf(body.get("remarks")) : null);
        fnf.setStatus("DRAFT");
        FullAndFinalSettlement saved = settlementRepository.save(fnf);
        record(ActivityActions.PAYROLL_FNF_CREATED, tenantId, storeId, ActivityResourceTypes.FNF,
                String.valueOf(saved.getId()), "Drafted full and final settlement");
        return saved;
    }

    @Transactional
    public FullAndFinalSettlement settle(Long tenantId, Long storeId, Long id) {
        FullAndFinalSettlement fnf = settlementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found."));
        requireStore(fnf.getTenantId(), fnf.getStoreId(), tenantId, storeId);
        fnf.setStatus("SETTLED");
        loanRepository.findByTenantIdAndStoreIdAndStaffIdAndStatus(tenantId, storeId, fnf.getStaffId(), "ACTIVE")
                .forEach(loan -> {
                    loan.setStatus("CLOSED");
                    loan.setOutstandingBalance(BigDecimal.ZERO);
                    loanRepository.save(loan);
                });
        personnelDetailsRepository.findById(fnf.getStaffId()).ifPresent(staff -> {
            staff.setActive(false);
            staff.setSkipPayroll(true);
            personnelDetailsRepository.save(staff);
        });
        record(ActivityActions.PAYROLL_FNF_SETTLED, tenantId, storeId, ActivityResourceTypes.FNF,
                String.valueOf(id), "Settled full and final");
        return settlementRepository.save(fnf);
    }

    public Map<String, Object> costReport(Long tenantId, Long storeId, String month, Integer year) {
        String salaryMonth = PayrollMonths.monthName(month);
        List<PersonnelPayslipHistory> slips = payslipHistoryRepository
                .findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYear(tenantId, storeId, salaryMonth, year);
        BigDecimal earnings = BigDecimal.ZERO;
        BigDecimal deductions = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (PersonnelPayslipHistory slip : slips) {
            earnings = earnings.add(nvl(slip.getTotalEarning()));
            deductions = deductions.add(nvl(slip.getTotalDeduction()));
            net = net.add(nvl(slip.getSalaryAmount()));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("staffId", slip.getStaffId());
            row.put("name", slip.getPersonnelName());
            row.put("employeeCode", slip.getEmployeeCode());
            row.put("earnings", slip.getTotalEarning());
            row.put("deductions", slip.getTotalDeduction());
            row.put("net", slip.getSalaryAmount());
            rows.add(row);
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("month", salaryMonth);
        report.put("year", year);
        report.put("headcount", slips.size());
        report.put("earnings", earnings);
        report.put("deductions", deductions);
        report.put("net", net);
        report.put("rows", rows);
        return report;
    }

    public String filingCsv(Long tenantId, Long storeId, String month, Integer year, String kind) {
        String salaryMonth = PayrollMonths.monthName(month);
        StatutoryConfig config = getStatutory(tenantId, storeId);
        List<PersonnelPayslipHistory> slips = payslipHistoryRepository
                .findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYear(tenantId, storeId, salaryMonth, year);
        StringBuilder csv = new StringBuilder();
        switch (kind) {
            case "esi" -> {
                csv.append("IP Number,Name,Employee Code,Days,Wages,EE,ER\n");
                for (PersonnelPayslipHistory slip : slips) {
                    PersonnelDetails staff = personnelDetailsRepository.findById(slip.getStaffId()).orElse(null);
                    BigDecimal wages = nvl(slip.getTotalEarning());
                    csv.append(csvCell(staff != null ? staff.getEsiNumber() : "")).append(',')
                            .append(csvCell(slip.getPersonnelName())).append(',')
                            .append(csvCell(slip.getEmployeeCode())).append(',')
                            .append(slip.getTotalPaidDays() != null ? slip.getTotalPaidDays() : "").append(',')
                            .append(wages).append(',')
                            .append(lineAmount(slip, "ESI Employee")).append(',')
                            .append(percent(wages, config.getEsiEmployerRate())).append('\n');
                }
            }
            case "pt" -> {
                csv.append("Name,Employee Code,Wages,Professional Tax\n");
                for (PersonnelPayslipHistory slip : slips) {
                    csv.append(csvCell(slip.getPersonnelName())).append(',')
                            .append(csvCell(slip.getEmployeeCode())).append(',')
                            .append(nvl(slip.getTotalEarning())).append(',')
                            .append(lineAmount(slip, "Professional Tax")).append('\n');
                }
            }
            case "tds" -> {
                csv.append("Name,PAN,Employee Code,Wages,TDS\n");
                for (PersonnelPayslipHistory slip : slips) {
                    PersonnelDetails staff = personnelDetailsRepository.findById(slip.getStaffId()).orElse(null);
                    csv.append(csvCell(slip.getPersonnelName())).append(',')
                            .append(csvCell(staff != null ? staff.getPanNumber() : slip.getPanNo())).append(',')
                            .append(csvCell(slip.getEmployeeCode())).append(',')
                            .append(nvl(slip.getTotalEarning())).append(',')
                            .append(lineAmount(slip, "TDS")).append('\n');
                }
            }
            default -> {
                csv.append("UAN,Name,Employee Code,Days,PF Wages,EE,ER\n");
                for (PersonnelPayslipHistory slip : slips) {
                    PersonnelDetails staff = personnelDetailsRepository.findById(slip.getStaffId()).orElse(null);
                    BigDecimal wages = nvl(slip.getTotalEarning()).min(nvl(config.getPfWageCeiling()));
                    csv.append(csvCell(staff != null ? staff.getUanNumber() : slip.getUanNumber())).append(',')
                            .append(csvCell(slip.getPersonnelName())).append(',')
                            .append(csvCell(slip.getEmployeeCode())).append(',')
                            .append(slip.getTotalPaidDays() != null ? slip.getTotalPaidDays() : "").append(',')
                            .append(wages).append(',')
                            .append(lineAmount(slip, "PF Employee")).append(',')
                            .append(percent(wages, config.getPfEmployerRate())).append('\n');
                }
            }
        }
        return csv.toString();
    }

    public String form16Html(Long tenantId, Long storeId, Long staffId, Integer year) {
        PersonnelDetails staff = personnelDetailsRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found."));
        int startYear = year != null ? year : LocalDate.now().getYear();
        List<PersonnelPayslipHistory> slips = new ArrayList<>();
        for (Month month : Month.values()) {
            int slipYear = month.getValue() >= 4 ? startYear : startYear + 1;
            payslipHistoryRepository.findByStaffIdAndSalaryMonthAndSalaryYear(staffId, month.name(), slipYear)
                    .ifPresent(slips::add);
        }
        BigDecimal earnings = slips.stream().map(s -> nvl(s.getTotalEarning())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deductions = slips.stream().map(s -> nvl(s.getTotalDeduction())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tds = slips.stream().map(s -> lineAmount(s, "TDS")).reduce(BigDecimal.ZERO, BigDecimal::add);
        String fy = startYear + "-" + (startYear + 1);
        return """
                <html><body style="font-family:Arial,sans-serif">
                <h2>Form 16 worksheet</h2>
                <p>%s %s · %s · FY %s</p>
                <table border="1" cellpadding="6" cellspacing="0">
                <tr><td>Gross earnings</td><td>%s</td></tr>
                <tr><td>Deductions</td><td>%s</td></tr>
                <tr><td>TDS deducted</td><td>%s</td></tr>
                <tr><td>PAN</td><td>%s</td></tr>
                <tr><td>Months included</td><td>%d</td></tr>
                </table>
                <p>This is a payroll worksheet, not a filed government form.</p>
                </body></html>
                """.formatted(
                nvlStr(staff.getFirstName()), nvlStr(staff.getLastName()), nvlStr(staff.getEmployeeCode()), fy,
                earnings, deductions, tds, nvlStr(staff.getPanNumber()), slips.size());
    }

    @Transactional
    public Map<String, Object> saveLogo(Long tenantId, Long storeId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a logo file.");
        }
        Files.createDirectories(logoRoot);
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "logo.png";
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".png";
        Path target = logoRoot.resolve(tenantId + "-" + storeId + ext);
        Files.write(target, file.getBytes());
        String dataUri = "data:" + (file.getContentType() != null ? file.getContentType() : "image/png")
                + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
        StoreProfileConfig config = storeProfileConfigRepository.findByTenantIdAndStoreId(tenantId, storeId)
                .orElseGet(() -> {
                    StoreProfileConfig created = new StoreProfileConfig();
                    created.setTenantId(tenantId);
                    created.setStoreId(storeId);
                    return created;
                });
        config.setLogoPath(dataUri);
        storeProfileConfigRepository.save(config);
        record(ActivityActions.PAYROLL_LOGO_SAVED, tenantId, storeId, ActivityResourceTypes.STORE,
                String.valueOf(storeId), "Updated store payslip logo");
        return Map.of("logoPath", dataUri, "fileName", target.getFileName().toString());
    }

    public Map<String, Object> logoStatus(Long tenantId, Long storeId) {
        return storeProfileConfigRepository.findByTenantIdAndStoreId(tenantId, storeId)
                .map(config -> Map.<String, Object>of("logoPath", config.getLogoPath() != null ? config.getLogoPath() : ""))
                .orElse(Map.of("logoPath", ""));
    }

    private BigDecimal lineAmount(PersonnelPayslipHistory slip, String suffix) {
        List<SalaryComponentDTO> lines = new ArrayList<>();
        if (slip.getDeductions() != null) {
            lines.addAll(slip.getDeductions());
        }
        return lines.stream()
                .filter(line -> line.getComponentName() != null && line.getComponentName().endsWith(suffix))
                .map(SalaryComponentDTO::getMonthlyValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireStore(Long rowTenant, Long rowStore, Long tenantId, Long storeId) {
        if (!Objects.equals(rowTenant, tenantId) || !Objects.equals(rowStore, storeId)) {
            throw new IllegalArgumentException("Record not found.");
        }
    }

    private void record(String action, Long tenantId, Long storeId, String type, String id, String summary) {
        activityRecorder.record(ActivityEvent.builder()
                .action(action)
                .tenantId(tenantId)
                .storeId(storeId)
                .resourceType(type)
                .resourceId(id)
                .summary(summary)
                .build());
    }

    private String currentFy() {
        LocalDate today = LocalDate.now();
        int start = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        return start + "-" + (start + 1);
    }

    private String csvCell(String value) {
        String text = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + text + "\"";
    }

    private String blank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String nvlStr(String value) {
        return value == null ? "" : value;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal percent(BigDecimal base, BigDecimal rate) {
        return nvl(base).multiply(nvl(rate)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal decimal(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private Long longVal(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Staff id is required.");
        }
        return Long.valueOf(String.valueOf(value));
    }
}
