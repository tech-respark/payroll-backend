package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityNames;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityResourceTypes;
import com.relfor.pcs.payroll.dto.HospitalityDtos;
import com.relfor.pcs.payroll.dto.SalaryComponentDTO;
import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.exceptions.ResourceNotFoundException;
import com.relfor.pcs.payroll.repository.*;
import com.relfor.pcs.payroll.security.Permissions;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HospitalityService {

    public static final String LINE_PREFIX = "Hosp: ";

    private final StaffAdvanceRepository advanceRepository;
    private final CommissionPlanRepository commissionPlanRepository;
    private final StaffSalesEntryRepository salesRepository;
    private final TipPoolRunRepository tipPoolRepository;
    private final StatutoryConfigRepository statutoryConfigRepository;
    private final StoreDailySalesRepository dailySalesRepository;
    private final FullAndFinalSettlementRepository fnfRepository;
    private final ShiftSwapRequestRepository swapRepository;
    private final StaffAvailabilityRepository availabilityRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final PersonnelPayslipHistoryRepository payslipHistoryRepository;
    private final PersonnelAttendanceRepository attendanceRepository;
    private final DayWiseAttendanceSummaryRepository daySummaryRepository;
    private final SStaffShiftsRepository staffShiftsRepository;
    private final MonthWiseAttendanceSummaryRepository monthSummaryRepository;
    private final WhatsappNotificationService whatsappNotificationService;
    private final StaffIdentityService staffIdentityService;
    private final ActivityRecorder activityRecorder;

    public HospitalityService(
            StaffAdvanceRepository advanceRepository,
            CommissionPlanRepository commissionPlanRepository,
            StaffSalesEntryRepository salesRepository,
            TipPoolRunRepository tipPoolRepository,
            StatutoryConfigRepository statutoryConfigRepository,
            StoreDailySalesRepository dailySalesRepository,
            FullAndFinalSettlementRepository fnfRepository,
            ShiftSwapRequestRepository swapRepository,
            StaffAvailabilityRepository availabilityRepository,
            PersonnelDetailsRepository personnelDetailsRepository,
            PersonnelPayslipHistoryRepository payslipHistoryRepository,
            PersonnelAttendanceRepository attendanceRepository,
            DayWiseAttendanceSummaryRepository daySummaryRepository,
            SStaffShiftsRepository staffShiftsRepository,
            MonthWiseAttendanceSummaryRepository monthSummaryRepository,
            WhatsappNotificationService whatsappNotificationService,
            StaffIdentityService staffIdentityService,
            ActivityRecorder activityRecorder) {
        this.advanceRepository = advanceRepository;
        this.commissionPlanRepository = commissionPlanRepository;
        this.salesRepository = salesRepository;
        this.tipPoolRepository = tipPoolRepository;
        this.statutoryConfigRepository = statutoryConfigRepository;
        this.dailySalesRepository = dailySalesRepository;
        this.fnfRepository = fnfRepository;
        this.swapRepository = swapRepository;
        this.availabilityRepository = availabilityRepository;
        this.personnelDetailsRepository = personnelDetailsRepository;
        this.payslipHistoryRepository = payslipHistoryRepository;
        this.attendanceRepository = attendanceRepository;
        this.daySummaryRepository = daySummaryRepository;
        this.staffShiftsRepository = staffShiftsRepository;
        this.monthSummaryRepository = monthSummaryRepository;
        this.whatsappNotificationService = whatsappNotificationService;
        this.staffIdentityService = staffIdentityService;
        this.activityRecorder = activityRecorder;
    }

    public List<StaffAdvance> listAdvances(Long tenantId, Long storeId, Long staffId) {
        if (staffId != null) {
            return advanceRepository.findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(tenantId, storeId, staffId);
        }
        return advanceRepository.findByTenantIdAndStoreIdOrderByIdDesc(tenantId, storeId);
    }

    @Transactional
    public StaffAdvance createAdvance(Long tenantId, Long storeId, HospitalityDtos.AdvanceRequest request) {
        requireStaff(request.getStaffId());
        if (request.getPrincipal() == null || request.getPrincipal().signum() <= 0) {
            throw new IllegalArgumentException("Principal must be greater than zero.");
        }
        StaffAdvance advance = new StaffAdvance();
        advance.setTenantId(tenantId);
        advance.setStoreId(storeId);
        advance.setStaffId(request.getStaffId());
        advance.setAdvanceType(defaultText(request.getAdvanceType(), "ADVANCE"));
        advance.setPrincipal(request.getPrincipal());
        BigDecimal installment = request.getInstallmentAmount() != null && request.getInstallmentAmount().signum() > 0
                ? request.getInstallmentAmount()
                : request.getPrincipal();
        advance.setInstallmentAmount(installment);
        advance.setOutstandingBalance(request.getPrincipal());
        advance.setRemainingInstallments(request.getRemainingInstallments() != null
                ? request.getRemainingInstallments()
                : 1);
        advance.setStatus("ACTIVE");
        LocalDate issued = request.getIssuedOn() != null ? request.getIssuedOn() : LocalDate.now();
        advance.setStartMonth(defaultText(request.getStartMonth(), issued.getMonth().name()));
        advance.setStartYear(request.getStartYear() != null ? request.getStartYear() : issued.getYear());
        advance.setIssuedOn(issued);
        advance.setRemarks(request.getRemarks());
        StaffAdvance saved = advanceRepository.save(advance);
        recordHospitality(ActivityActions.HOSPITALITY_ADVANCE_CREATED, ActivityResourceTypes.ADVANCE, saved.getId(),
                staffLabel(saved.getStaffId()), "Issued an advance", tenantId, storeId);
        return saved;
    }

    @Transactional
    public StaffAdvance closeAdvance(Long tenantId, Long storeId, Long id) {
        StaffAdvance advance = requireOwnedAdvance(tenantId, storeId, id);
        advance.setStatus("CLOSED");
        advance.setOutstandingBalance(BigDecimal.ZERO);
        advance.setRemainingInstallments(0);
        StaffAdvance saved = advanceRepository.save(advance);
        recordHospitality(ActivityActions.HOSPITALITY_ADVANCE_CLOSED, ActivityResourceTypes.ADVANCE, saved.getId(),
                staffLabel(saved.getStaffId()), "Closed an advance", tenantId, storeId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CommissionPlan> listCommissionPlans(Long tenantId, Long storeId) {
        return commissionPlanRepository.findByTenantIdAndStoreId(tenantId, storeId);
    }

    @Transactional
    public CommissionPlan saveCommissionPlan(Long tenantId, Long storeId, HospitalityDtos.CommissionPlanRequest request) {
        if (request.getPlanName() == null || request.getPlanName().isBlank()) {
            throw new IllegalArgumentException("Plan name is required.");
        }
        CommissionPlan plan = new CommissionPlan();
        plan.setTenantId(tenantId);
        plan.setStoreId(storeId);
        plan.setStaffId(request.getStaffId());
        plan.setPlanName(request.getPlanName().trim());
        plan.setCategory(defaultText(request.getCategory(), "SERVICE").toUpperCase(Locale.ROOT));
        plan.setActive(request.getActive() == null || request.getActive());
        if (request.getSlabs() != null) {
            for (HospitalityDtos.SlabRequest slabRequest : request.getSlabs()) {
                CommissionSlab slab = new CommissionSlab();
                slab.setPlan(plan);
                slab.setThresholdFrom(nvl(slabRequest.getThresholdFrom()));
                slab.setThresholdTo(slabRequest.getThresholdTo());
                slab.setRatePercent(nvl(slabRequest.getRatePercent()));
                plan.getSlabs().add(slab);
            }
        }
        CommissionPlan saved = commissionPlanRepository.save(plan);
        recordHospitality(ActivityActions.HOSPITALITY_COMMISSION_SAVED, ActivityResourceTypes.COMMISSION_PLAN, saved.getId(),
                saved.getPlanName(), "Saved commission plan " + saved.getPlanName(), tenantId, storeId);
        return saved;
    }

    @Transactional
    public void deleteCommissionPlan(Long tenantId, Long storeId, Long id) {
        CommissionPlan plan = commissionPlanRepository.findWithSlabsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission plan not found."));
        if (!Objects.equals(plan.getTenantId(), tenantId) || !Objects.equals(plan.getStoreId(), storeId)) {
            throw new ResourceNotFoundException("Commission plan not found.");
        }
        commissionPlanRepository.delete(plan);
        recordHospitality(ActivityActions.HOSPITALITY_COMMISSION_DELETED, ActivityResourceTypes.COMMISSION_PLAN, plan.getId(),
                plan.getPlanName(), "Deleted commission plan " + plan.getPlanName(), tenantId, storeId);
    }

    public List<StaffSalesEntry> listSales(Long tenantId, Long storeId, LocalDate from, LocalDate to, Long staffId) {
        YearMonth period = YearMonth.now();
        LocalDate start = from != null ? from : period.atDay(1);
        LocalDate end = to != null ? to : period.atEndOfMonth();
        if (staffId != null) {
            return salesRepository.findByTenantIdAndStoreIdAndStaffIdAndSaleDateBetween(tenantId, storeId, staffId, start, end);
        }
        return salesRepository.findByTenantIdAndStoreIdAndSaleDateBetween(tenantId, storeId, start, end);
    }

    @Transactional
    public StaffSalesEntry createSale(Long tenantId, Long storeId, HospitalityDtos.SalesRequest request) {
        requireStaff(request.getStaffId());
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Sale amount must be greater than zero.");
        }
        StaffSalesEntry entry = new StaffSalesEntry();
        entry.setTenantId(tenantId);
        entry.setStoreId(storeId);
        entry.setStaffId(request.getStaffId());
        entry.setSaleDate(request.getSaleDate() != null ? request.getSaleDate() : LocalDate.now());
        entry.setCategory(defaultText(request.getCategory(), "SERVICE").toUpperCase(Locale.ROOT));
        entry.setAmount(request.getAmount());
        entry.setSource(defaultText(request.getSource(), "MANUAL"));
        entry.setExternalRef(request.getExternalRef());
        entry.setJobCode(request.getJobCode());
        entry.setNotes(request.getNotes());
        StaffSalesEntry saved = salesRepository.save(entry);
        if (!"CSV".equalsIgnoreCase(saved.getSource())) {
            recordHospitality(ActivityActions.HOSPITALITY_SALE_RECORDED, ActivityResourceTypes.SALES_ENTRY, saved.getId(),
                    staffLabel(saved.getStaffId()), "Recorded a sale", tenantId, storeId);
        }
        return saved;
    }

    @Transactional
    public List<StaffSalesEntry> importSales(Long tenantId, Long storeId, HospitalityDtos.SalesImportRequest request) {
        if (request.getRows() == null || request.getRows().isEmpty()) {
            throw new IllegalArgumentException("Import rows are required.");
        }
        List<StaffSalesEntry> saved = new ArrayList<>();
        for (HospitalityDtos.SalesRequest row : request.getRows()) {
            row.setSource(defaultText(row.getSource(), "CSV"));
            saved.add(createSale(tenantId, storeId, row));
        }
        recordHospitality(ActivityActions.HOSPITALITY_SALES_IMPORTED, ActivityResourceTypes.SALES_ENTRY, null,
                ActivityNames.countLabel(saved.size(), "sales row", "sales rows"),
                "Imported " + ActivityNames.countLabel(saved.size(), "sales row", "sales rows"), tenantId, storeId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<TipPoolRun> listTipPools(Long tenantId, Long storeId) {
        return tipPoolRepository.findByTenantIdAndStoreIdOrderByPeriodFromDesc(tenantId, storeId);
    }

    @Transactional
    public TipPoolRun createTipPool(Long tenantId, Long storeId, HospitalityDtos.TipPoolRequest request) {
        if (request.getPoolAmount() == null || request.getPoolAmount().signum() <= 0) {
            throw new IllegalArgumentException("Pool amount must be greater than zero.");
        }
        if (request.getPeriodFrom() == null || request.getPeriodTo() == null) {
            throw new IllegalArgumentException("Period from and to are required.");
        }
        TipPoolRun run = new TipPoolRun();
        run.setTenantId(tenantId);
        run.setStoreId(storeId);
        run.setPeriodFrom(request.getPeriodFrom());
        run.setPeriodTo(request.getPeriodTo());
        run.setPoolAmount(request.getPoolAmount());
        run.setMethod(defaultText(request.getMethod(), "HOURS").toUpperCase(Locale.ROOT));
        run.setStatus("DRAFT");
        run.setRemarks(request.getRemarks());
        TipPoolRun saved = tipPoolRepository.save(run);
        recordHospitality(ActivityActions.HOSPITALITY_TIP_POOL_CREATED, ActivityResourceTypes.TIP_POOL, saved.getId(),
                ActivityNames.formatDate(saved.getPeriodFrom()) + " to " + ActivityNames.formatDate(saved.getPeriodTo()),
                "Created a tip pool", tenantId, storeId);
        return saved;
    }

    @Transactional
    public TipPoolRun distributeTipPool(Long tenantId, Long storeId, Long id, HospitalityDtos.TipPoolRequest request) {
        TipPoolRun run = tipPoolRepository.findWithSharesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tip pool not found."));
        if (!Objects.equals(run.getTenantId(), tenantId) || !Objects.equals(run.getStoreId(), storeId)) {
            throw new ResourceNotFoundException("Tip pool not found.");
        }
        run.getShares().clear();
        List<PersonnelDetails> staff = personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId);
        Map<Long, BigDecimal> weights = new LinkedHashMap<>();
        if ("POINTS".equalsIgnoreCase(run.getMethod()) && request != null && request.getPoints() != null) {
            for (Map<String, Object> row : request.getPoints()) {
                Long staffId = toLong(row.get("staffId"));
                BigDecimal points = toDecimal(row.get("points"));
                if (staffId != null && points != null && points.signum() > 0) {
                    weights.put(staffId, points);
                }
            }
        } else if ("ROLE".equalsIgnoreCase(run.getMethod())) {
            for (PersonnelDetails person : staff) {
                if (Boolean.FALSE.equals(person.getActive())) {
                    continue;
                }
                weights.put(person.getId(), BigDecimal.ONE);
            }
        } else {
            List<DayWiseAttendanceSummary> days = daySummaryRepository.findByTenantAndStoreAndDateBetween(
                    tenantId, storeId, run.getPeriodFrom(), run.getPeriodTo());
            for (DayWiseAttendanceSummary day : days) {
                BigDecimal hours = day.getSumOfActualHoursWorkedInADay() != null
                        ? day.getSumOfActualHoursWorkedInADay()
                        : nvl(day.getTotalHoursWorkedInADay());
                if (hours.signum() <= 0) {
                    continue;
                }
                weights.merge(day.getStaffId(), hours, BigDecimal::add);
            }
        }
        BigDecimal totalWeight = weights.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.signum() <= 0) {
            throw new IllegalArgumentException("No hours, points, or eligible staff found for this pool.");
        }
        for (Map.Entry<Long, BigDecimal> entry : weights.entrySet()) {
            TipPoolShare share = new TipPoolShare();
            share.setTipPool(run);
            share.setStaffId(entry.getKey());
            share.setHoursWorked(entry.getValue());
            share.setPoints(entry.getValue());
            share.setShareAmount(run.getPoolAmount()
                    .multiply(entry.getValue())
                    .divide(totalWeight, 2, RoundingMode.HALF_UP));
            run.getShares().add(share);
        }
        run.setStatus("DISTRIBUTED");
        TipPoolRun saved = tipPoolRepository.save(run);
        recordHospitality(ActivityActions.HOSPITALITY_TIP_POOL_DISTRIBUTED, ActivityResourceTypes.TIP_POOL, saved.getId(),
                saved.getPeriodFrom() + " to " + saved.getPeriodTo(), "Distributed a tip pool", tenantId, storeId);
        return saved;
    }

    public StatutoryConfig getStatutory(Long tenantId, Long storeId) {
        return statutoryConfigRepository.findByTenantIdAndStoreId(tenantId, storeId)
                .orElseGet(() -> defaultStatutory(tenantId, storeId));
    }

    @Transactional
    public StatutoryConfig saveStatutory(Long tenantId, Long storeId, HospitalityDtos.StatutoryRequest request) {
        StatutoryConfig config = statutoryConfigRepository.findByTenantIdAndStoreId(tenantId, storeId)
                .orElseGet(() -> defaultStatutory(tenantId, storeId));
        if (request.getPfEnabled() != null) config.setPfEnabled(request.getPfEnabled());
        if (request.getEsiEnabled() != null) config.setEsiEnabled(request.getEsiEnabled());
        if (request.getPtEnabled() != null) config.setPtEnabled(request.getPtEnabled());
        if (request.getTdsEnabled() != null) config.setTdsEnabled(request.getTdsEnabled());
        if (request.getLwfEnabled() != null) config.setLwfEnabled(request.getLwfEnabled());
        if (request.getPfWageCeiling() != null) config.setPfWageCeiling(request.getPfWageCeiling());
        if (request.getEsiWageCeiling() != null) config.setEsiWageCeiling(request.getEsiWageCeiling());
        if (request.getPfEmployeeRate() != null) config.setPfEmployeeRate(request.getPfEmployeeRate());
        if (request.getPfEmployerRate() != null) config.setPfEmployerRate(request.getPfEmployerRate());
        if (request.getEsiEmployeeRate() != null) config.setEsiEmployeeRate(request.getEsiEmployeeRate());
        if (request.getEsiEmployerRate() != null) config.setEsiEmployerRate(request.getEsiEmployerRate());
        if (request.getPtAmount() != null) config.setPtAmount(request.getPtAmount());
        if (request.getLwfEmployeeAmount() != null) config.setLwfEmployeeAmount(request.getLwfEmployeeAmount());
        if (request.getLwfEmployerAmount() != null) config.setLwfEmployerAmount(request.getLwfEmployerAmount());
        if (request.getTdsRate() != null) config.setTdsRate(request.getTdsRate());
        StatutoryConfig saved = statutoryConfigRepository.save(config);
        recordHospitality(ActivityActions.HOSPITALITY_STATUTORY_SAVED, ActivityResourceTypes.STORE, storeId,
                "Statutory", "Updated statutory payroll settings", tenantId, storeId);
        return saved;
    }

    public List<StoreDailySales> listDailySales(Long tenantId, Long storeId, LocalDate from, LocalDate to) {
        YearMonth period = YearMonth.now();
        return dailySalesRepository.findByTenantIdAndStoreIdAndSaleDateBetween(
                tenantId, storeId, from != null ? from : period.atDay(1), to != null ? to : period.atEndOfMonth());
    }

    @Transactional
    public StoreDailySales upsertDailySales(Long tenantId, Long storeId, HospitalityDtos.DailySalesRequest request) {
        if (request.getSaleDate() == null) {
            throw new IllegalArgumentException("Sale date is required.");
        }
        StoreDailySales row = dailySalesRepository.findByTenantIdAndStoreIdAndSaleDate(tenantId, storeId, request.getSaleDate())
                .orElseGet(StoreDailySales::new);
        row.setTenantId(tenantId);
        row.setStoreId(storeId);
        row.setSaleDate(request.getSaleDate());
        row.setNetSales(nvl(request.getNetSales()));
        row.setCovers(request.getCovers());
        row.setNotes(request.getNotes());
        StoreDailySales saved = dailySalesRepository.save(row);
        recordHospitality(ActivityActions.HOSPITALITY_DAILY_SALES_SAVED, ActivityResourceTypes.STORE, storeId,
                ActivityNames.formatDate(saved.getSaleDate()),
                "Saved daily sales for " + ActivityNames.formatDate(saved.getSaleDate()), tenantId, storeId);
        return saved;
    }

    public List<Map<String, Object>> laborReport(Long tenantId, Long storeId, LocalDate from, LocalDate to) {
        YearMonth period = YearMonth.now();
        LocalDate start = from != null ? from : period.atDay(1);
        LocalDate end = to != null ? to : period.atEndOfMonth();
        Map<LocalDate, StoreDailySales> salesByDate = dailySalesRepository
                .findByTenantIdAndStoreIdAndSaleDateBetween(tenantId, storeId, start, end)
                .stream()
                .collect(Collectors.toMap(StoreDailySales::getSaleDate, row -> row, (a, b) -> a));
        List<DayWiseAttendanceSummary> days = daySummaryRepository.findByTenantAndStoreAndDateBetween(tenantId, storeId, start, end);
        Map<Long, PersonnelDetails> staffById = personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId)
                .stream().collect(Collectors.toMap(PersonnelDetails::getId, p -> p, (a, b) -> a));
        Map<LocalDate, List<DayWiseAttendanceSummary>> byDate = days.stream()
                .collect(Collectors.groupingBy(DayWiseAttendanceSummary::getAttendanceDate));
        List<Map<String, Object>> rows = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            List<DayWiseAttendanceSummary> dayRows = byDate.getOrDefault(cursor, List.of());
            BigDecimal labor = BigDecimal.ZERO;
            BigDecimal hours = BigDecimal.ZERO;
            for (DayWiseAttendanceSummary day : dayRows) {
                BigDecimal worked = day.getSumOfActualHoursWorkedInADay() != null
                        ? day.getSumOfActualHoursWorkedInADay()
                        : nvl(day.getTotalHoursWorkedInADay());
                hours = hours.add(worked);
                PersonnelDetails person = staffById.get(day.getStaffId());
                labor = labor.add(worked.multiply(hourlyRate(person)));
            }
            StoreDailySales sales = salesByDate.get(cursor);
            BigDecimal netSales = sales != null ? nvl(sales.getNetSales()) : BigDecimal.ZERO;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("saleDate", cursor.toString());
            row.put("netSales", netSales);
            row.put("covers", sales != null ? sales.getCovers() : null);
            row.put("laborCost", labor.setScale(2, RoundingMode.HALF_UP));
            row.put("hoursWorked", hours.setScale(2, RoundingMode.HALF_UP));
            row.put("laborPercent", netSales.signum() > 0
                    ? labor.multiply(BigDecimal.valueOf(100)).divide(netSales, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            rows.add(row);
            cursor = cursor.plusDays(1);
        }
        return rows;
    }

    public List<Map<String, Object>> musterRoll(Long tenantId, Long storeId, String month, Integer year) {
        YearMonth period = yearMonth(month, year);
        List<DayWiseAttendanceSummary> days = daySummaryRepository.findByTenantAndStoreAndDateBetween(
                tenantId, storeId, period.atDay(1), period.atEndOfMonth());
        Map<Long, PersonnelDetails> staffById = personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId)
                .stream().collect(Collectors.toMap(PersonnelDetails::getId, p -> p, (a, b) -> a));
        Map<Long, List<DayWiseAttendanceSummary>> byStaff = days.stream()
                .collect(Collectors.groupingBy(DayWiseAttendanceSummary::getStaffId));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<Long, List<DayWiseAttendanceSummary>> entry : byStaff.entrySet()) {
            PersonnelDetails person = staffById.get(entry.getKey());
            Map<String, String> marks = new LinkedHashMap<>();
            long present = 0;
            long absent = 0;
            for (DayWiseAttendanceSummary day : entry.getValue()) {
                String mark = "A";
                if (Boolean.TRUE.equals(day.getIsOnLeave())) {
                    mark = "L";
                } else if (Boolean.TRUE.equals(day.getIsWeeklyOff())) {
                    mark = "WO";
                } else if (Boolean.TRUE.equals(day.getIsHoliday())) {
                    mark = "H";
                } else if (Boolean.TRUE.equals(day.getIsPresent())) {
                    mark = DayWiseAttendanceSummary.DayCategory.SPLIT.equals(day.getDayCategory()) ? "SP" : "P";
                    present++;
                } else {
                    absent++;
                }
                marks.put(day.getAttendanceDate().toString(), mark);
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("staffId", entry.getKey());
            row.put("name", person != null ? displayName(person) : String.valueOf(entry.getKey()));
            row.put("employeeCode", person != null ? person.getEmployeeCode() : null);
            BigDecimal hours = entry.getValue().stream()
                    .map(day -> day.getSumOfActualHoursWorkedInADay() != null
                            ? day.getSumOfActualHoursWorkedInADay()
                            : nvl(day.getTotalHoursWorkedInADay()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            row.put("presentDays", present);
            row.put("absentDays", absent);
            row.put("hoursWorked", hours.setScale(2, RoundingMode.HALF_UP));
            row.put("marks", marks);
            rows.add(row);
        }
        return rows;
    }

    public String neftCsv(Long tenantId, Long storeId, String month, Integer year) {
        String salaryMonth = monthName(month);
        List<PersonnelPayslipHistory> slips = payslipHistoryRepository
                .findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYear(tenantId, storeId, salaryMonth, year);
        StringBuilder csv = new StringBuilder("employeeCode,name,accountNumber,ifsc,bankName,netPay,month,year\n");
        for (PersonnelPayslipHistory slip : slips) {
            csv.append(csvCell(slip.getEmployeeCode())).append(',')
                    .append(csvCell(slip.getPersonnelName())).append(',')
                    .append(csvCell(slip.getAccountNumber())).append(',')
                    .append(csvCell(slip.getIfscCode())).append(',')
                    .append(csvCell(slip.getBankName())).append(',')
                    .append(nvl(slip.getSalaryAmount()).toPlainString()).append(',')
                    .append(csvCell(slip.getSalaryMonth())).append(',')
                    .append(slip.getSalaryYear())
                    .append('\n');
        }
        return csv.toString();
    }

    public List<FullAndFinalSettlement> listFnf(Long tenantId, Long storeId) {
        return fnfRepository.findByTenantIdAndStoreIdOrderByIdDesc(tenantId, storeId);
    }

    @Transactional
    public FullAndFinalSettlement createFnf(Long tenantId, Long storeId, HospitalityDtos.FnfRequest request) {
        PersonnelDetails staff = requireStaff(request.getStaffId());
        if (request.getLastWorkingDate() == null) {
            throw new IllegalArgumentException("Last working date is required.");
        }
        BigDecimal monthly = nvl(staff.getSalaryAmount());
        int daysInMonth = request.getLastWorkingDate().lengthOfMonth();
        BigDecimal unpaid = monthly
                .multiply(BigDecimal.valueOf(request.getLastWorkingDate().getDayOfMonth()))
                .divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);
        BigDecimal advanceRecovery = advanceRepository
                .findByTenantIdAndStoreIdAndStaffIdAndStatus(tenantId, storeId, staff.getId(), "ACTIVE")
                .stream()
                .map(StaffAdvance::getOutstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal leaveEncashment = nvl(request.getLeaveEncashment());
        BigDecimal other = nvl(request.getOtherDeductions());
        FullAndFinalSettlement fnf = new FullAndFinalSettlement();
        fnf.setTenantId(tenantId);
        fnf.setStoreId(storeId);
        fnf.setStaffId(staff.getId());
        fnf.setLastWorkingDate(request.getLastWorkingDate());
        fnf.setStatus("DRAFT");
        fnf.setUnpaidSalary(unpaid);
        fnf.setLeaveEncashment(leaveEncashment);
        fnf.setAdvanceRecovery(advanceRecovery);
        fnf.setOtherDeductions(other);
        fnf.setNetPayable(unpaid.add(leaveEncashment).subtract(advanceRecovery).subtract(other));
        fnf.setRemarks(request.getRemarks());
        FullAndFinalSettlement saved = fnfRepository.save(fnf);
        recordHospitality(ActivityActions.HOSPITALITY_FNF_CREATED, ActivityResourceTypes.FNF, saved.getId(),
                staffLabel(saved.getStaffId()), "Created full and final for " + staffLabel(saved.getStaffId()), tenantId, storeId);
        return saved;
    }

    @Transactional
    public FullAndFinalSettlement settleFnf(Long tenantId, Long storeId, Long id) {
        FullAndFinalSettlement fnf = fnfRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found."));
        if (!Objects.equals(fnf.getTenantId(), tenantId) || !Objects.equals(fnf.getStoreId(), storeId)) {
            throw new ResourceNotFoundException("Settlement not found.");
        }
        fnf.setStatus("SETTLED");
        for (StaffAdvance advance : advanceRepository.findByTenantIdAndStoreIdAndStaffIdAndStatus(
                tenantId, storeId, fnf.getStaffId(), "ACTIVE")) {
            advance.setStatus("CLOSED");
            advance.setOutstandingBalance(BigDecimal.ZERO);
            advance.setRemainingInstallments(0);
            advanceRepository.save(advance);
        }
        personnelDetailsRepository.findById(fnf.getStaffId()).ifPresent(staff -> {
            staff.setActive(false);
            personnelDetailsRepository.save(staff);
        });
        FullAndFinalSettlement saved = fnfRepository.save(fnf);
        recordHospitality(ActivityActions.HOSPITALITY_FNF_SETTLED, ActivityResourceTypes.FNF, saved.getId(),
                staffLabel(saved.getStaffId()), "Settled full and final for " + staffLabel(saved.getStaffId()), tenantId, storeId);
        return saved;
    }

    public List<ShiftSwapRequest> listSwaps(Long tenantId, Long storeId) {
        if (SecurityUtils.hasAuthority(Permissions.MANAGE_SHIFTS)
                || SecurityUtils.hasAuthority(Permissions.MANAGE_HOSPITALITY)) {
            return swapRepository.findByTenantIdAndStoreIdOrderByIdDesc(tenantId, storeId);
        }
        Long self = SecurityUtils.getStaffId();
        if (self == null) {
            return List.of();
        }
        return swapRepository.findInvolvingStaff(tenantId, storeId, self);
    }

    @Transactional
    public ShiftSwapRequest createSwap(Long tenantId, Long storeId, HospitalityDtos.SwapRequest request) {
        Long self = SecurityUtils.getStaffId();
        if (self == null) {
            throw new IllegalArgumentException("Authenticated staff id is required.");
        }
        if (request.getTargetStaffId() == null || request.getShiftDate() == null) {
            throw new IllegalArgumentException("Target staff and shift date are required.");
        }
        if (Objects.equals(self, request.getTargetStaffId())) {
            throw new IllegalArgumentException("Choose another staff member to swap with.");
        }
        requireStaff(request.getTargetStaffId());
        ShiftSwapRequest swap = new ShiftSwapRequest();
        swap.setTenantId(tenantId);
        swap.setStoreId(storeId);
        swap.setRequesterStaffId(self);
        swap.setTargetStaffId(request.getTargetStaffId());
        swap.setShiftDate(request.getShiftDate());
        swap.setStatus("PENDING");
        swap.setReason(request.getReason());
        ShiftSwapRequest saved = swapRepository.save(swap);
        recordHospitality(ActivityActions.HOSPITALITY_SWAP_REQUESTED, ActivityResourceTypes.SHIFT_SWAP, saved.getId(),
                staffLabel(self) + " → " + staffLabel(request.getTargetStaffId()),
                "Requested a shift swap", tenantId, storeId);
        return saved;
    }

    @Transactional
    public ShiftSwapRequest decideSwap(Long tenantId, Long storeId, Long id, boolean approved) {
        ShiftSwapRequest swap = swapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Swap request not found."));
        if (!Objects.equals(swap.getTenantId(), tenantId) || !Objects.equals(swap.getStoreId(), storeId)) {
            throw new ResourceNotFoundException("Swap request not found.");
        }
        if (!"PENDING".equals(swap.getStatus())) {
            throw new IllegalArgumentException("Only pending swaps can be decided.");
        }
        swap.setDecidedBy(SecurityUtils.getStaffId());
        if (!approved) {
            swap.setStatus("REJECTED");
            ShiftSwapRequest saved = swapRepository.save(swap);
            recordHospitality(ActivityActions.HOSPITALITY_SWAP_DECIDED, ActivityResourceTypes.SHIFT_SWAP, saved.getId(),
                    staffLabel(saved.getRequesterStaffId()), "Rejected a shift swap", tenantId, storeId);
            return saved;
        }
        List<SStaffShifts> requester = rosteredShifts(tenantId, storeId, swap.getShiftDate(), swap.getRequesterStaffId());
        List<SStaffShifts> target = rosteredShifts(tenantId, storeId, swap.getShiftDate(), swap.getTargetStaffId());
        if (requester.isEmpty() && target.isEmpty()) {
            throw new IllegalArgumentException("Neither staff member has a rostered shift on that date.");
        }
        Long targetWriteKey = writeKey(swap.getTargetStaffId());
        Long requesterWriteKey = writeKey(swap.getRequesterStaffId());
        for (SStaffShifts row : requester) {
            row.setStaffId(targetWriteKey);
            staffShiftsRepository.save(row);
        }
        for (SStaffShifts row : target) {
            row.setStaffId(requesterWriteKey);
            staffShiftsRepository.save(row);
        }
        swap.setStatus("APPROVED");
        ShiftSwapRequest saved = swapRepository.save(swap);
        recordHospitality(ActivityActions.HOSPITALITY_SWAP_DECIDED, ActivityResourceTypes.SHIFT_SWAP, saved.getId(),
                staffLabel(saved.getRequesterStaffId()),
                approved ? "Approved a shift swap" : "Rejected a shift swap", tenantId, storeId);
        return saved;
    }

    public List<StaffAvailability> listAvailability(Long tenantId, Long storeId, Long staffId) {
        Long scoped = staffId != null ? staffId : SecurityUtils.getStaffId();
        if (scoped == null) {
            return List.of();
        }
        SecurityUtils.requireSelfOrAnyAuthority(scoped, Permissions.MANAGE_SHIFTS, Permissions.MANAGE_HOSPITALITY, Permissions.VIEW_OTHER_STAFF);
        return availabilityRepository.findByTenantIdAndStoreIdAndStaffId(tenantId, storeId, scoped);
    }

    @Transactional
    public StaffAvailability saveAvailability(Long tenantId, Long storeId, HospitalityDtos.AvailabilityRequest request) {
        Long self = SecurityUtils.getStaffId();
        if (self == null) {
            throw new IllegalArgumentException("Authenticated staff id is required.");
        }
        if (request.getWeekday() == null || request.getWeekday().isBlank()) {
            throw new IllegalArgumentException("Weekday is required.");
        }
        String weekday = request.getWeekday().trim().toUpperCase(Locale.ROOT);
        StaffAvailability row = availabilityRepository
                .findByTenantIdAndStoreIdAndStaffIdAndWeekday(tenantId, storeId, self, weekday)
                .orElseGet(StaffAvailability::new);
        row.setTenantId(tenantId);
        row.setStoreId(storeId);
        row.setStaffId(self);
        row.setWeekday(weekday);
        row.setAvailableFrom(request.getAvailableFrom());
        row.setAvailableTo(request.getAvailableTo());
        row.setAvailable(request.getAvailable() == null || request.getAvailable());
        StaffAvailability saved = availabilityRepository.save(row);
        recordHospitality(ActivityActions.HOSPITALITY_AVAILABILITY_SAVED, ActivityResourceTypes.STAFF, self,
                staffLabel(self), "Updated availability for " + weekday, tenantId, storeId);
        return saved;
    }

    @Transactional
    public PersonnelAttendance clock(Long tenantId, Long storeId, HospitalityDtos.ClockRequest request) {
        Long self = SecurityUtils.getStaffId();
        if (self == null) {
            throw new IllegalArgumentException("Authenticated staff id is required.");
        }
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(zone);
        String event = defaultText(request.getPunchEvent(), inferPunchEvent(tenantId, storeId, self, today)).toUpperCase(Locale.ROOT);
        LocalDate attendanceDate = today;
        if ("OUT".equals(event)) {
            List<PersonnelAttendance> todayPunches = attendanceRepository
                    .findByTenantIdAndStoreIdAndStaffIdAndAttendanceDateOrderByPunchTimestampAsc(tenantId, storeId, self, today);
            if (todayPunches.size() % 2 == 0) {
                LocalDate yesterday = today.minusDays(1);
                List<PersonnelAttendance> yesterdayPunches = attendanceRepository
                        .findByTenantIdAndStoreIdAndStaffIdAndAttendanceDateOrderByPunchTimestampAsc(tenantId, storeId, self, yesterday);
                if (yesterdayPunches.size() % 2 == 1) {
                    attendanceDate = yesterday;
                }
            }
        }
        List<PersonnelAttendance> dayPunches = attendanceRepository
                .findByTenantIdAndStoreIdAndStaffIdAndAttendanceDateOrderByPunchTimestampAsc(tenantId, storeId, self, attendanceDate);
        PersonnelAttendance punch = new PersonnelAttendance();
        punch.setTenantId(tenantId);
        punch.setStoreId(storeId);
        punch.setStaffId(self);
        punch.setAttendanceDate(attendanceDate);
        punch.setAttendanceDayOfWeek(attendanceDate.getDayOfWeek().name());
        punch.setPunchEvent(event);
        punch.setPunchTimestamp(now);
        punch.setUploadSource("HOSPITALITY_MOBILE");
        punch.setCurrentStatus("APPROVED");
        punch.setCreatedTimestamp(now);
        punch.setCreatedBy(self);
        punch.setSequenceNumberOfPunch(dayPunches.size() + 1);
        String remark = request.getNote();
        if (request.getLatitude() != null && request.getLongitude() != null) {
            String geo = "geo:" + request.getLatitude() + "," + request.getLongitude();
            remark = remark == null || remark.isBlank() ? geo : remark + " " + geo;
        }
        punch.setRemark(remark);
        PersonnelAttendance saved = attendanceRepository.save(punch);
        recordHospitality(ActivityActions.ATTENDANCE_CLOCKED, ActivityResourceTypes.PUNCH, saved.getId(),
                staffLabel(self), "Clocked " + event.toLowerCase(Locale.ROOT), tenantId, storeId);
        return saved;
    }

    public Map<String, Object> mySummary(Long tenantId, Long storeId) {
        Long self = SecurityUtils.getStaffId();
        if (self == null) {
            throw new IllegalArgumentException("Authenticated staff id is required.");
        }
        YearMonth period = YearMonth.now();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("staffId", self);
        summary.put("advances", advanceRepository.findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(tenantId, storeId, self));
        summary.put("sales", salesRepository.findByTenantIdAndStoreIdAndStaffIdAndSaleDateBetween(
                tenantId, storeId, self, period.atDay(1), period.atEndOfMonth()));
        summary.put("availability", availabilityRepository.findByTenantIdAndStoreIdAndStaffId(tenantId, storeId, self));
        summary.put("swaps", swapRepository.findInvolvingStaff(tenantId, storeId, self));
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        summary.put("todayPunches", attendanceRepository
                .findByTenantIdAndStoreIdAndStaffIdAndAttendanceDateOrderByPunchTimestampAsc(tenantId, storeId, self, today));
        List<SStaffShifts> todayRoster = rosteredShifts(tenantId, storeId, today, self);
        if (todayRoster.isEmpty()) {
            summary.put("todayShift", null);
        } else {
            SStaffShifts shift = todayRoster.get(0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("slot", shift.getSlot());
            row.put("weeklyOff", shift.getWeeklyOff());
            row.put("onLeave", shift.getOnLeave());
            summary.put("todayShift", row);
        }
        summary.put("teammates", personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId).stream()
                .filter(staff -> staff.getActive() == null || Boolean.TRUE.equals(staff.getActive()))
                .map(staff -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", staff.getId());
                    row.put("firstName", staff.getFirstName());
                    row.put("lastName", staff.getLastName());
                    return row;
                })
                .toList());
        return summary;
    }

    @Transactional
    public Map<String, Object> applyPayroll(Long tenantId, Long storeId, String month, Integer year) {
        String salaryMonth = monthName(month);
        List<PersonnelDetails> staff = personnelDetailsRepository.findByApplicationTenantIdAndStoreId(tenantId, storeId);
        int applied = 0;
        for (PersonnelDetails person : staff) {
            if (applyToPayslip(person.getId(), salaryMonth, year, tenantId, storeId) != null) {
                applied++;
            }
        }
        recordHospitality(ActivityActions.PAYROLL_APPLIED, ActivityResourceTypes.PAYROLL_RUN, salaryMonth + "-" + year,
                salaryMonth + " " + year, "Applied hospitality payroll for " + salaryMonth + " " + year, tenantId, storeId);
        return Map.of("applied", applied, "month", salaryMonth, "year", year);
    }

    @Transactional
    public PersonnelPayslipHistory applyToPayslip(Long staffId, String month, Integer year, Long tenantId, Long storeId) {
        String salaryMonth = monthName(month);
        PersonnelPayslipHistory payslip = payslipHistoryRepository
                .findByStaffIdAndSalaryMonthAndSalaryYear(staffId, salaryMonth, year)
                .orElse(null);
        if (payslip == null) {
            return null;
        }
        PersonnelDetails staff = personnelDetailsRepository.findById(staffId).orElse(null);
        if (staff == null) {
            return null;
        }
        YearMonth period = yearMonth(salaryMonth, year);
        List<SalaryComponentDTO> earnings = new ArrayList<>(payslip.getEarnings() != null ? payslip.getEarnings() : List.of());
        List<SalaryComponentDTO> deductions = new ArrayList<>(payslip.getDeductions() != null ? payslip.getDeductions() : List.of());
        earnings.removeIf(this::isHospitalityLine);
        deductions.removeIf(this::isHospitalityLine);

        addEarning(earnings, "Cycle Pay", cyclePay(staff, payslip));
        addEarning(earnings, "Overtime", overtimePay(staff, payslip));
        Map<String, BigDecimal> commissions = commissionForMonth(tenantId, storeId, staff, period);
        commissions.forEach((name, amount) -> addEarning(earnings, name, amount));
        addEarning(earnings, "Service Charge Share", tipShare(tenantId, storeId, staffId, period));

        BigDecimal advance = recoverAdvance(tenantId, storeId, staffId, salaryMonth, year);
        addDeduction(deductions, "Advance Recovery", advance);

        payslip.setEarnings(earnings);
        payslip.setDeductions(deductions);
        BigDecimal totalEarning = sumLines(earnings);
        BigDecimal totalDeduction = sumLines(deductions);
        payslip.setTotalEarning(totalEarning);
        payslip.setTotalDeduction(totalDeduction);
        payslip.setSalaryAmount(totalEarning.subtract(totalDeduction));
        return payslipHistoryRepository.save(payslip);
    }

    public void notifyPayslip(Long staffId, String month, Integer year) {
        personnelDetailsRepository.findById(staffId).ifPresent(staff -> {
            PersonnelPayslipHistory payslip = payslipHistoryRepository
                    .findByStaffIdAndSalaryMonthAndSalaryYear(staffId, monthName(month), year)
                    .orElse(null);
            if (payslip != null) {
                whatsappNotificationService.sendPayslipViaWhatsapp(staff, payslip);
            }
        });
    }

    private BigDecimal cyclePay(PersonnelDetails staff, PersonnelPayslipHistory payslip) {
        String cycle = defaultText(staff.getPayCycle(), "MONTHLY").toUpperCase(Locale.ROOT);
        String type = defaultText(staff.getEmploymentType(), "SALARIED").toUpperCase(Locale.ROOT);
        BigDecimal amount = nvl(staff.getSalaryAmount());
        if (amount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        if ("MONTHLY".equals(cycle) && ("SALARIED".equals(type) || "CONSULTANT".equals(type))) {
            return BigDecimal.ZERO;
        }
        BigDecimal paidDays = payslip.getTotalPaidDays() != null ? BigDecimal.valueOf(payslip.getTotalPaidDays()) : BigDecimal.ZERO;
        if ("DAILY".equals(cycle) || "DAILY_WAGE".equals(type)) {
            return amount.multiply(paidDays).setScale(2, RoundingMode.HALF_UP);
        }
        if ("WEEKLY".equals(cycle)) {
            return amount.multiply(paidDays).divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);
        }
        if ("HOURLY".equals(cycle) || "HOURLY".equals(type)) {
            BigDecimal hours = paidDays.multiply(BigDecimal.valueOf(staff.getWorkingHours() != null ? staff.getWorkingHours() : 9.5f));
            return amount.multiply(hours).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal overtimePay(PersonnelDetails staff, PersonnelPayslipHistory payslip) {
        MonthWiseAttendanceSummary summary = monthSummaryRepository.getSummaryByStaffIdAndMonth(
                staff.getId(), payslip.getSalaryMonth(), payslip.getSalaryYear() != null ? payslip.getSalaryYear().longValue() : null);
        if (summary == null || summary.getTotalOvertimeMins() == null || summary.getTotalOvertimeMins() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal hourly = hourlyRate(staff);
        BigDecimal multiplier = staff.getOvertimeMultiplier() != null ? staff.getOvertimeMultiplier() : new BigDecimal("2.00");
        BigDecimal otHours = BigDecimal.valueOf(summary.getTotalOvertimeMins()).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return hourly.multiply(otHours).multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> commissionForMonth(Long tenantId, Long storeId, PersonnelDetails staff, YearMonth period) {
        List<StaffSalesEntry> sales = salesRepository.findByTenantIdAndStoreIdAndStaffIdAndSaleDateBetween(
                tenantId, storeId, staff.getId(), period.atDay(1), period.atEndOfMonth());
        Map<String, BigDecimal> totals = new HashMap<>();
        for (StaffSalesEntry sale : sales) {
            totals.merge(sale.getCategory(), sale.getAmount(), BigDecimal::add);
        }
        List<CommissionPlan> plans = commissionPlanRepository.findByTenantIdAndStoreIdAndActiveTrue(tenantId, storeId);
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> entry : totals.entrySet()) {
            CommissionPlan plan = plans.stream()
                    .filter(p -> entry.getKey().equalsIgnoreCase(p.getCategory()))
                    .filter(p -> p.getStaffId() == null || Objects.equals(p.getStaffId(), staff.getId()))
                    .max(Comparator.comparing(p -> p.getStaffId() != null ? 1 : 0))
                    .orElse(null);
            if (plan == null || plan.getSlabs() == null || plan.getSlabs().isEmpty()) {
                continue;
            }
            CommissionSlab slab = plan.getSlabs().stream()
                    .filter(s -> entry.getValue().compareTo(nvl(s.getThresholdFrom())) >= 0)
                    .filter(s -> s.getThresholdTo() == null || entry.getValue().compareTo(s.getThresholdTo()) < 0)
                    .max(Comparator.comparing(s -> nvl(s.getThresholdFrom())))
                    .orElse(null);
            if (slab == null) {
                continue;
            }
            BigDecimal amount = entry.getValue()
                    .multiply(nvl(slab.getRatePercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            result.put(plan.getCategory() + " Commission", amount);
        }
        return result;
    }

    private BigDecimal tipShare(Long tenantId, Long storeId, Long staffId, YearMonth period) {
        return tipPoolRepository
                .findByTenantIdAndStoreIdAndStatusAndPeriodFromLessThanEqualAndPeriodToGreaterThanEqual(
                        tenantId, storeId, "DISTRIBUTED", period.atEndOfMonth(), period.atDay(1))
                .stream()
                .flatMap(run -> {
                    TipPoolRun loaded = tipPoolRepository.findWithSharesById(run.getId()).orElse(run);
                    return loaded.getShares().stream();
                })
                .filter(share -> Objects.equals(share.getStaffId(), staffId))
                .map(TipPoolShare::getShareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal recoverAdvance(Long tenantId, Long storeId, Long staffId, String month, Integer year) {
        List<StaffAdvance> advances = advanceRepository.findByTenantIdAndStoreIdAndStaffIdAndStatus(
                tenantId, storeId, staffId, "ACTIVE");
        BigDecimal recovered = BigDecimal.ZERO;
        for (StaffAdvance advance : advances) {
            if (advance.getStartYear() != null && year != null && advance.getStartYear() > year) {
                continue;
            }
            if (month.equals(advance.getLastRecoveredMonth()) && Objects.equals(year, advance.getLastRecoveredYear())) {
                recovered = recovered.add(nvl(advance.getLastRecoveredAmount()));
                continue;
            }
            BigDecimal installment = advance.getInstallmentAmount().min(advance.getOutstandingBalance());
            if (installment.signum() <= 0) {
                continue;
            }
            advance.setOutstandingBalance(advance.getOutstandingBalance().subtract(installment));
            if (advance.getRemainingInstallments() != null) {
                advance.setRemainingInstallments(Math.max(0, advance.getRemainingInstallments() - 1));
            }
            if (advance.getOutstandingBalance().signum() <= 0
                    || (advance.getRemainingInstallments() != null && advance.getRemainingInstallments() == 0)) {
                advance.setStatus("CLOSED");
                advance.setOutstandingBalance(BigDecimal.ZERO);
            }
            advance.setLastRecoveredMonth(month);
            advance.setLastRecoveredYear(year);
            advance.setLastRecoveredAmount(installment);
            advanceRepository.save(advance);
            recovered = recovered.add(installment);
        }
        return recovered;
    }

    private Map<String, BigDecimal> statutoryDeductions(Long tenantId, Long storeId, PersonnelPayslipHistory payslip, List<SalaryComponentDTO> earnings) {
        StatutoryConfig config = getStatutory(tenantId, storeId);
        BigDecimal wage = sumLines(earnings);
        if (wage.signum() <= 0) {
            wage = nvl(payslip.getTotalEarning());
        }
        Map<String, BigDecimal> deductions = new LinkedHashMap<>();
        if (Boolean.TRUE.equals(config.getPfEnabled())) {
            BigDecimal base = wage.min(nvl(config.getPfWageCeiling()));
            deductions.put("PF Employee", percent(base, config.getPfEmployeeRate()));
        }
        if (Boolean.TRUE.equals(config.getEsiEnabled()) && wage.compareTo(nvl(config.getEsiWageCeiling())) <= 0) {
            deductions.put("ESI Employee", percent(wage, config.getEsiEmployeeRate()));
        }
        if (Boolean.TRUE.equals(config.getPtEnabled())) {
            deductions.put("Professional Tax", nvl(config.getPtAmount()));
        }
        if (Boolean.TRUE.equals(config.getTdsEnabled()) && config.getTdsRate() != null && config.getTdsRate().signum() > 0) {
            deductions.put("TDS", percent(wage, config.getTdsRate()));
        }
        if (Boolean.TRUE.equals(config.getLwfEnabled())) {
            deductions.put("LWF Employee", nvl(config.getLwfEmployeeAmount()));
        }
        return deductions;
    }

    private void addEarning(List<SalaryComponentDTO> lines, String name, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return;
        }
        lines.add(hospitalityLine(name, amount));
    }

    private void addDeduction(List<SalaryComponentDTO> lines, String name, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return;
        }
        lines.add(hospitalityLine(name, amount));
    }

    private SalaryComponentDTO hospitalityLine(String name, BigDecimal amount) {
        SalaryComponentDTO line = new SalaryComponentDTO();
        line.setComponentName(LINE_PREFIX + name);
        line.setMonthlyValue(amount.setScale(2, RoundingMode.HALF_UP));
        line.setComponentType("FORMULA");
        line.setCalculatedMonthly(true);
        return line;
    }

    private boolean isHospitalityLine(SalaryComponentDTO line) {
        return line != null && line.getComponentName() != null && line.getComponentName().startsWith(LINE_PREFIX);
    }

    private BigDecimal sumLines(List<SalaryComponentDTO> lines) {
        return lines.stream()
                .map(SalaryComponentDTO::getMonthlyValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal hourlyRate(PersonnelDetails staff) {
        if (staff == null) {
            return BigDecimal.ZERO;
        }
        if (staff.getSecondaryHourlyRate() != null && staff.getSecondaryHourlyRate().signum() > 0
                && "HOURLY".equalsIgnoreCase(staff.getEmploymentType())) {
            return staff.getSecondaryHourlyRate();
        }
        BigDecimal monthly = nvl(staff.getSalaryAmount());
        float hours = staff.getWorkingHours() != null && staff.getWorkingHours() > 0 ? staff.getWorkingHours() : 9.5f;
        String cycle = defaultText(staff.getPayCycle(), "MONTHLY").toUpperCase(Locale.ROOT);
        if ("HOURLY".equalsIgnoreCase(staff.getEmploymentType()) || "HOURLY".equals(cycle)) {
            return monthly;
        }
        if ("DAILY".equals(cycle) || "DAILY_WAGE".equalsIgnoreCase(staff.getEmploymentType())) {
            return monthly.divide(BigDecimal.valueOf(hours), 4, RoundingMode.HALF_UP);
        }
        if ("WEEKLY".equals(cycle)) {
            return monthly.divide(BigDecimal.valueOf(hours * 6d), 4, RoundingMode.HALF_UP);
        }
        return monthly.divide(BigDecimal.valueOf(30d * hours), 4, RoundingMode.HALF_UP);
    }

    private StatutoryConfig defaultStatutory(Long tenantId, Long storeId) {
        StatutoryConfig config = new StatutoryConfig();
        config.setTenantId(tenantId);
        config.setStoreId(storeId);
        return config;
    }

    private PersonnelDetails requireStaff(Long staffId) {
        if (staffId == null) {
            throw new IllegalArgumentException("Staff id is required.");
        }
        return personnelDetailsRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found."));
    }

    private StaffAdvance requireOwnedAdvance(Long tenantId, Long storeId, Long id) {
        StaffAdvance advance = advanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance not found."));
        if (!Objects.equals(advance.getTenantId(), tenantId) || !Objects.equals(advance.getStoreId(), storeId)) {
            throw new ResourceNotFoundException("Advance not found.");
        }
        return advance;
    }

    private String inferPunchEvent(Long tenantId, Long storeId, Long staffId, LocalDate today) {
        List<PersonnelAttendance> punches = attendanceRepository
                .findByTenantIdAndStoreIdAndStaffIdAndAttendanceDateOrderByPunchTimestampAsc(tenantId, storeId, staffId, today);
        if (punches.size() % 2 == 0) {
            List<PersonnelAttendance> yesterday = attendanceRepository
                    .findByTenantIdAndStoreIdAndStaffIdAndAttendanceDateOrderByPunchTimestampAsc(tenantId, storeId, staffId, today.minusDays(1));
            return yesterday.size() % 2 == 1 ? "OUT" : "IN";
        }
        return "OUT";
    }

    private List<SStaffShifts> rosteredShifts(Long tenantId, Long storeId, LocalDate date, Long staffId) {
        List<Long> keys = staffIdentityService.rosterKeysForStaffId(staffId);
        if (keys.isEmpty()) {
            return List.of();
        }
        return staffShiftsRepository.findByTenantIdAndStoreIdAndShiftDateAndStaffIdIn(tenantId, storeId, date, keys);
    }

    private Long writeKey(Long staffId) {
        List<Long> keys = staffIdentityService.rosterKeysForStaffId(staffId);
        return keys.isEmpty() ? staffId : keys.get(0);
    }

    private YearMonth yearMonth(String month, Integer year) {
        int resolvedYear = year != null ? year : LocalDate.now().getYear();
        return YearMonth.of(resolvedYear, Month.valueOf(monthName(month)));
    }

    private String monthName(String month) {
        if (month == null || month.isBlank()) {
            return LocalDate.now().getMonth().name();
        }
        return Month.valueOf(month.trim().toUpperCase(Locale.ROOT)).name();
    }

    private BigDecimal percent(BigDecimal base, BigDecimal rate) {
        return nvl(base).multiply(nvl(rate)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void recordHospitality(String action, String resourceType, Object resourceId, String resourceLabel,
                                  String summary, Long tenantId, Long storeId) {
        activityRecorder.record(ActivityEvent.builder()
                .action(action)
                .tenantId(tenantId)
                .storeId(storeId)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .resourceLabel(resourceLabel)
                .summary(summary)
                .build());
    }

    private String staffLabel(Long staffId) {
        if (staffId == null) {
            return "staff";
        }
        String name = ActivityNames.displayName(personnelDetailsRepository.findById(staffId).orElse(null));
        return name != null ? name : "staff " + staffId;
    }

    private String displayName(PersonnelDetails person) {
        String first = person.getFirstName() != null ? person.getFirstName() : "";
        String last = person.getLastName() != null ? person.getLastName() : "";
        return (first + " " + last).trim();
    }

    private String csvCell(String value) {
        String text = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + text + "\"";
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }

    private BigDecimal toDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            return new BigDecimal(text);
        }
        return null;
    }
}
