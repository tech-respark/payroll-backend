package com.relfor.pcs.payroll.service.payroll;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityResourceTypes;
import com.relfor.pcs.payroll.entity.PayRun;
import com.relfor.pcs.payroll.entity.PayRunSkip;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.PersonnelPayslipHistory;
import com.relfor.pcs.payroll.entity.StoreDetails;
import com.relfor.pcs.payroll.repository.PayRunRepository;
import com.relfor.pcs.payroll.repository.PayRunSkipRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.PersonnelPayslipHistoryRepository;
import com.relfor.pcs.payroll.repository.StoreDetailsRepository;
import com.relfor.pcs.payroll.service.HospitalityService;
import com.relfor.pcs.payroll.service.SalaryCalculation;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PayRunService {

    private final PayRunRepository payRunRepository;
    private final PayRunSkipRepository skipRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final PersonnelPayslipHistoryRepository payslipHistoryRepository;
    private final StoreDetailsRepository storeDetailsRepository;
    private final SalaryCalculation salaryCalculation;
    private final HospitalityService hospitalityService;
    private final ActivityRecorder activityRecorder;

    public PayRunService(PayRunRepository payRunRepository,
                         PayRunSkipRepository skipRepository,
                         PersonnelDetailsRepository personnelDetailsRepository,
                         PersonnelPayslipHistoryRepository payslipHistoryRepository,
                         StoreDetailsRepository storeDetailsRepository,
                         SalaryCalculation salaryCalculation,
                         HospitalityService hospitalityService,
                         ActivityRecorder activityRecorder) {
        this.payRunRepository = payRunRepository;
        this.skipRepository = skipRepository;
        this.personnelDetailsRepository = personnelDetailsRepository;
        this.payslipHistoryRepository = payslipHistoryRepository;
        this.storeDetailsRepository = storeDetailsRepository;
        this.salaryCalculation = salaryCalculation;
        this.hospitalityService = hospitalityService;
        this.activityRecorder = activityRecorder;
    }

    @Transactional
    public Map<String, Object> getOrCreate(Long tenantId, Long storeId, String month, Integer year, String cycleType) {
        String salaryMonth = PayrollMonths.monthName(month);
        String cycle = cycleType == null || cycleType.isBlank() ? "REGULAR" : cycleType.toUpperCase();
        PayRun run = payRunRepository
                .findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYearAndCycleType(tenantId, storeId, salaryMonth, year, cycle)
                .orElseGet(() -> {
                    PayRun created = new PayRun();
                    created.setTenantId(tenantId);
                    created.setStoreId(storeId);
                    created.setSalaryMonth(salaryMonth);
                    created.setSalaryYear(year);
                    created.setCycleType(cycle);
                    created.setStatus("DRAFT");
                    return payRunRepository.save(created);
                });
        return detail(run);
    }

    public Map<String, Object> find(Long tenantId, Long storeId, String month, Integer year, String cycleType) {
        String salaryMonth = PayrollMonths.monthName(month);
        String cycle = cycleType == null || cycleType.isBlank() ? "REGULAR" : cycleType.toUpperCase();
        return payRunRepository
                .findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYearAndCycleType(tenantId, storeId, salaryMonth, year, cycle)
                .map(this::detail)
                .orElseGet(() -> unopened(salaryMonth, year, cycle));
    }

    public List<PayRun> list(Long tenantId, Long storeId) {
        return payRunRepository.findByTenantIdAndStoreIdOrderBySalaryYearDescIdDesc(tenantId, storeId);
    }

    public Map<String, Object> get(Long tenantId, Long storeId, Long runId) {
        return detail(require(runId, tenantId, storeId));
    }

    @Transactional
    public Map<String, Object> calculate(Long tenantId, Long storeId, Long runId) {
        PayRun run = requireOpen(runId, tenantId, storeId);
        String month = run.getSalaryMonth();
        Integer year = run.getSalaryYear();
        Set<Long> skipped = skipRepository.findByPayRunId(run.getId()).stream()
                .map(PayRunSkip::getStaffId)
                .collect(Collectors.toSet());
        List<PersonnelDetails> staff = personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId);
        int calculated = 0;
        int skippedCount = 0;
        for (PersonnelDetails person : staff) {
            if (person.getActive() != null && !person.getActive()) {
                continue;
            }
            if (Boolean.TRUE.equals(person.getSkipPayroll()) || skipped.contains(person.getId())) {
                skippedCount++;
                continue;
            }
            salaryCalculation.processSalary(person.getId(), month, year != null ? year.longValue() : null, tenantId, storeId, false);
            calculated++;
        }
        run.setStaffCount(staff.size());
        run.setCalculatedCount(calculated);
        run.setSkippedCount(skippedCount);
        run.setStatus("CALCULATED");
        run.setCalculatedAt(Instant.now());
        payRunRepository.save(run);
        record(ActivityActions.PAYROLL_RUN_CALCULATED, run, "Calculated pay run for " + month + " " + year);
        return detail(run);
    }

    @Transactional
    public Map<String, Object> skip(Long tenantId, Long storeId, Long runId, Long staffId, String reason, boolean remove) {
        PayRun run = requireOpen(runId, tenantId, storeId);
        if (remove) {
            skipRepository.deleteByPayRunIdAndStaffId(run.getId(), staffId);
        } else {
            PayRunSkip skip = skipRepository.findByPayRunIdAndStaffId(run.getId(), staffId).orElseGet(PayRunSkip::new);
            skip.setPayRunId(run.getId());
            skip.setStaffId(staffId);
            skip.setReason(reason);
            skipRepository.save(skip);
        }
        run.setSkippedCount(skipRepository.findByPayRunId(run.getId()).size());
        payRunRepository.save(run);
        return detail(run);
    }

    @Transactional
    public Map<String, Object> submit(Long tenantId, Long storeId, Long runId) {
        PayRun run = requireStatus(runId, tenantId, storeId, "CALCULATED");
        run.setStatus("PENDING_APPROVAL");
        run.setSubmittedAt(Instant.now());
        run.setSubmittedBy(SecurityUtils.currentUsername());
        payRunRepository.save(run);
        record(ActivityActions.PAYROLL_RUN_SUBMITTED, run, "Submitted pay run for approval");
        return detail(run);
    }

    @Transactional
    public Map<String, Object> approve(Long tenantId, Long storeId, Long runId) {
        PayRun run = requireStatus(runId, tenantId, storeId, "PENDING_APPROVAL");
        run.setStatus("APPROVED");
        run.setApprovedAt(Instant.now());
        run.setApprovedBy(SecurityUtils.currentUsername());
        payRunRepository.save(run);
        record(ActivityActions.PAYROLL_RUN_APPROVED, run, "Approved pay run");
        return detail(run);
    }

    @Transactional
    public Map<String, Object> publish(Long tenantId, Long storeId, Long runId) {
        PayRun run = requireStatus(runId, tenantId, storeId, "APPROVED");
        String month = run.getSalaryMonth();
        Integer year = run.getSalaryYear();
        Set<Long> skipped = skipRepository.findByPayRunId(run.getId()).stream()
                .map(PayRunSkip::getStaffId)
                .collect(Collectors.toSet());
        List<PersonnelDetails> staff = personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId);
        for (PersonnelDetails person : staff) {
            if (Boolean.TRUE.equals(person.getSkipPayroll()) || skipped.contains(person.getId())) {
                continue;
            }
            hospitalityService.notifyPayslip(person.getId(), month, year);
        }
        run.setStatus("PUBLISHED");
        run.setPublishedAt(Instant.now());
        run.setPublishedBy(SecurityUtils.currentUsername());
        payRunRepository.save(run);
        record(ActivityActions.PAYROLL_RUN_PUBLISHED, run, "Published payslips for " + month + " " + year);
        return detail(run);
    }

    @Transactional
    public Map<String, Object> lock(Long tenantId, Long storeId, Long runId) {
        PayRun run = require(runId, tenantId, storeId);
        if (!"APPROVED".equals(run.getStatus()) && !"PUBLISHED".equals(run.getStatus())) {
            throw new IllegalStateException("Approve the run before locking the period.");
        }
        StoreDetails store = storeDetailsRepository.findById(storeId).orElse(null);
        if (store != null) {
            Month month = Month.valueOf(run.getSalaryMonth());
            store.setPayrollLockedUpToDate(LocalDate.of(run.getSalaryYear(), month, month.length(false)));
            storeDetailsRepository.save(store);
        }
        run.setStatus("LOCKED");
        run.setLockedAt(Instant.now());
        payRunRepository.save(run);
        record(ActivityActions.PAYROLL_RUN_LOCKED, run, "Locked payroll period " + run.getSalaryMonth() + " " + run.getSalaryYear());
        return detail(run);
    }

    private Map<String, Object> unopened(String month, Integer year, String cycleType) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", null);
        view.put("month", month);
        view.put("year", year);
        view.put("cycleType", cycleType);
        view.put("status", "UNOPENED");
        view.put("staffCount", 0);
        view.put("calculatedCount", 0);
        view.put("skippedCount", 0);
        view.put("skips", List.of());
        view.put("lines", List.of());
        return view;
    }

    public Map<String, Object> detail(PayRun run) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", run.getId());
        view.put("month", run.getSalaryMonth());
        view.put("year", run.getSalaryYear());
        view.put("cycleType", run.getCycleType());
        view.put("status", run.getStatus());
        view.put("staffCount", liveStaffCount(run));
        view.put("calculatedCount", run.getCalculatedCount());
        view.put("submittedBy", run.getSubmittedBy());
        view.put("approvedBy", run.getApprovedBy());
        view.put("publishedBy", run.getPublishedBy());
        view.put("calculatedAt", run.getCalculatedAt());
        view.put("submittedAt", run.getSubmittedAt());
        view.put("approvedAt", run.getApprovedAt());
        view.put("publishedAt", run.getPublishedAt());
        view.put("lockedAt", run.getLockedAt());
        view.put("notes", run.getNotes());
        Set<Long> skipped = new HashSet<>();
        List<Map<String, Object>> skipRows = new ArrayList<>();
        for (PayRunSkip skip : skipRepository.findByPayRunId(run.getId())) {
            skipped.add(skip.getStaffId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("staffId", skip.getStaffId());
            row.put("reason", skip.getReason());
            skipRows.add(row);
        }
        view.put("skips", skipRows);
        view.put("skippedCount", skipRows.isEmpty() ? run.getSkippedCount() : skipRows.size());
        List<PersonnelPayslipHistory> slips = payslipHistoryRepository
                .findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYear(run.getTenantId(), run.getStoreId(), run.getSalaryMonth(), run.getSalaryYear());
        List<Map<String, Object>> lines = new ArrayList<>();
        for (PersonnelPayslipHistory slip : slips) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("staffId", slip.getStaffId());
            line.put("name", slip.getPersonnelName());
            line.put("employeeCode", slip.getEmployeeCode());
            line.put("earnings", slip.getTotalEarning());
            line.put("deductions", slip.getTotalDeduction());
            line.put("net", slip.getSalaryAmount());
            line.put("skipped", skipped.contains(slip.getStaffId()));
            lines.add(line);
        }
        view.put("lines", lines);
        return view;
    }

    private int liveStaffCount(PayRun run) {
        if (run.getStaffCount() != null && run.getStaffCount() > 0) {
            return run.getStaffCount();
        }
        return (int) personnelDetailsRepository.findByApplicationTenantIdAndStoreId(run.getTenantId(), run.getStoreId())
                .stream()
                .filter(person -> person.getActive() == null || Boolean.TRUE.equals(person.getActive()))
                .count();
    }

    private PayRun require(Long runId, Long tenantId, Long storeId) {
        PayRun run = payRunRepository.findById(runId).orElseThrow(() -> new IllegalArgumentException("Pay run not found."));
        if (!tenantId.equals(run.getTenantId()) || !storeId.equals(run.getStoreId())) {
            throw new IllegalArgumentException("Pay run not found.");
        }
        return run;
    }

    private PayRun requireOpen(Long runId, Long tenantId, Long storeId) {
        PayRun run = require(runId, tenantId, storeId);
        if ("APPROVED".equals(run.getStatus()) || "PUBLISHED".equals(run.getStatus()) || "LOCKED".equals(run.getStatus())) {
            throw new IllegalStateException("This pay run is closed. Calculate again only while it is draft, calculated, or pending approval.");
        }
        return run;
    }

    private PayRun requireStatus(Long runId, Long tenantId, Long storeId, String status) {
        PayRun run = require(runId, tenantId, storeId);
        if (!status.equals(run.getStatus())) {
            throw new IllegalStateException("Pay run must be " + status + " first.");
        }
        return run;
    }

    private void record(String action, PayRun run, String summary) {
        activityRecorder.record(ActivityEvent.builder()
                .action(action)
                .tenantId(run.getTenantId())
                .storeId(run.getStoreId())
                .resourceType(ActivityResourceTypes.PAYROLL_RUN)
                .resourceId(String.valueOf(run.getId()))
                .resourceLabel(run.getSalaryMonth() + " " + run.getSalaryYear())
                .summary(summary)
                .build());
    }
}
