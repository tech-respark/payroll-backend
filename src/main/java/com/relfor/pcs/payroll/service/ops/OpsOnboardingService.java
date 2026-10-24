package com.relfor.pcs.payroll.service.ops;

import com.relfor.pcs.payroll.dto.PayrollSchedulerInfo;
import com.relfor.pcs.payroll.entity.LeavePlan;
import com.relfor.pcs.payroll.entity.LeavePlanRule;
import com.relfor.pcs.payroll.entity.LeaveType;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.PlatformProduct;
import com.relfor.pcs.payroll.entity.Role;
import com.relfor.pcs.payroll.entity.SShiftsSlots;
import com.relfor.pcs.payroll.entity.StatutoryConfig;
import com.relfor.pcs.payroll.entity.StoreDetails;
import com.relfor.pcs.payroll.entity.StoreHoliday;
import com.relfor.pcs.payroll.entity.StoreProfileConfig;
import com.relfor.pcs.payroll.entity.StoreStaffRole;
import com.relfor.pcs.payroll.entity.SupportSession;
import com.relfor.pcs.payroll.entity.TenantCompanyMapping;
import com.relfor.pcs.payroll.entity.TenantSubscription;
import com.relfor.pcs.payroll.entity.TenantSubscriptionEvent;
import com.relfor.pcs.payroll.entity.TerminalDetails;
import com.relfor.pcs.payroll.repository.LeavePlanRepository;
import com.relfor.pcs.payroll.repository.LeavePlanRuleRepository;
import com.relfor.pcs.payroll.repository.LeaveTypeRepository;
import com.relfor.pcs.payroll.repository.PayrollSchedulerInfoRepo;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.PlatformUserRepository;
import com.relfor.pcs.payroll.repository.RoleRepository;
import com.relfor.pcs.payroll.repository.SShiftsSlotsRepository;
import com.relfor.pcs.payroll.repository.SalaryComponentDefinitionsRepository;
import com.relfor.pcs.payroll.repository.StatutoryConfigRepository;
import com.relfor.pcs.payroll.repository.StoreDetailsRepository;
import com.relfor.pcs.payroll.repository.StoreHolidayRepository;
import com.relfor.pcs.payroll.repository.StoreProfileConfigRepository;
import com.relfor.pcs.payroll.repository.StoreStaffRoleRepository;
import com.relfor.pcs.payroll.repository.SupportSessionRepository;
import com.relfor.pcs.payroll.repository.TenantCompanyMappingRepository;
import com.relfor.pcs.payroll.repository.TenantSubscriptionEventRepository;
import com.relfor.pcs.payroll.repository.TenantSubscriptionRepository;
import com.relfor.pcs.payroll.security.CustomUserDetails;
import com.relfor.pcs.payroll.security.CustomUserDetailsService;
import com.relfor.pcs.payroll.security.JwtUtil;
import com.relfor.pcs.payroll.security.Permissions;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OpsOnboardingService {

    private static final String ADMIN_PERMISSIONS = String.join(",",
            Permissions.VIEW_STAFF, Permissions.MANAGE_STAFF, Permissions.VIEW_OTHER_STAFF,
            Permissions.VIEW_SHIFTS, Permissions.MANAGE_SHIFTS, Permissions.MANAGE_STORE_SETTINGS,
            Permissions.VIEW_ATTENDANCE, Permissions.MANAGE_ATTENDANCE,
            Permissions.VIEW_SALARY, Permissions.MANAGE_SALARY,
            Permissions.VIEW_ROLES, Permissions.MANAGE_ROLES, Permissions.VIEW_ACTIVITY_LOG,
            Permissions.MANAGE_LEAVES, Permissions.VIEW_REPORTS, Permissions.VIEW_HR_DASHBOARD);

    private static final String HOSPITALITY_PERMISSIONS = String.join(",",
            Permissions.VIEW_HOSPITALITY, Permissions.MANAGE_HOSPITALITY);
    private final TenantCompanyMappingRepository tenantRepository;
    private final StoreDetailsRepository storeRepository;
    private final StoreProfileConfigRepository profileRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final TenantSubscriptionEventRepository subscriptionEventRepository;
    private final PersonnelDetailsRepository personnelRepository;
    private final RoleRepository roleRepository;
    private final StoreStaffRoleRepository storeStaffRoleRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeavePlanRepository leavePlanRepository;
    private final LeavePlanRuleRepository leavePlanRuleRepository;
    private final SalaryComponentDefinitionsRepository salaryDefinitionsRepository;
    private final SShiftsSlotsRepository shiftSlotsRepository;
    private final PayrollSchedulerInfoRepo schedulerInfoRepo;
    private final StatutoryConfigRepository statutoryConfigRepository;
    private final PlatformUserRepository platformUserRepository;
    private final StoreHolidayRepository holidayRepository;
    private final SupportSessionRepository supportSessionRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final OpsLicenseGuard licenseGuard;
    private final OpsSalaryPackService salaryPackService;
    private final OpsProductService productService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OpsOnboardingService(
            TenantCompanyMappingRepository tenantRepository,
            StoreDetailsRepository storeRepository,
            StoreProfileConfigRepository profileRepository,
            TenantSubscriptionRepository subscriptionRepository,
            TenantSubscriptionEventRepository subscriptionEventRepository,
            PersonnelDetailsRepository personnelRepository,
            RoleRepository roleRepository,
            StoreStaffRoleRepository storeStaffRoleRepository,
            LeaveTypeRepository leaveTypeRepository,
            LeavePlanRepository leavePlanRepository,
            LeavePlanRuleRepository leavePlanRuleRepository,
            SalaryComponentDefinitionsRepository salaryDefinitionsRepository,
            SShiftsSlotsRepository shiftSlotsRepository,
            PayrollSchedulerInfoRepo schedulerInfoRepo,
            StatutoryConfigRepository statutoryConfigRepository,
            PlatformUserRepository platformUserRepository,
            StoreHolidayRepository holidayRepository,
            SupportSessionRepository supportSessionRepository,
            CustomUserDetailsService userDetailsService,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            OpsLicenseGuard licenseGuard,
            OpsSalaryPackService salaryPackService,
            OpsProductService productService) {
        this.tenantRepository = tenantRepository;
        this.storeRepository = storeRepository;
        this.profileRepository = profileRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionEventRepository = subscriptionEventRepository;
        this.personnelRepository = personnelRepository;
        this.roleRepository = roleRepository;
        this.storeStaffRoleRepository = storeStaffRoleRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leavePlanRepository = leavePlanRepository;
        this.leavePlanRuleRepository = leavePlanRuleRepository;
        this.salaryDefinitionsRepository = salaryDefinitionsRepository;
        this.shiftSlotsRepository = shiftSlotsRepository;
        this.schedulerInfoRepo = schedulerInfoRepo;
        this.statutoryConfigRepository = statutoryConfigRepository;
        this.platformUserRepository = platformUserRepository;
        this.holidayRepository = holidayRepository;
        this.supportSessionRepository = supportSessionRepository;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.licenseGuard = licenseGuard;
        this.salaryPackService = salaryPackService;
        this.productService = productService;
    }

    public void assertCanAccess(Long tenantId) {
        requireAccessibleTenant(tenantId);
    }

    public List<Map<String, Object>> listTenants() {
        List<Map<String, Object>> all = tenantRepository.findAll().stream().map(this::toListItem).toList();
        if (SecurityUtils.hasAuthority(Permissions.SUPERADMIN)) {
            return all;
        }
        Long ownerId = SecurityUtils.currentPlatformUserId();
        return all.stream().filter((item) -> ownsSubscription(item.get("subscription"), ownerId)).toList();
    }

    public Map<String, Object> getTenant(Long tenantId) {
        TenantCompanyMapping tenant = requireAccessibleTenant(tenantId);
        Map<String, Object> detail = toListItem(tenant);
        List<StoreDetails> stores = storeRepository.fetchStoreDetailListForTenant(tenantId, 0L);
        detail.put("stores", stores.stream().map(this::toStoreView).toList());
        detail.put("admins", personnelRepository.findByApplicationTenantId(tenantId).stream()
                .map(this::toStaffView)
                .toList());
        TenantSubscription subscription = subscriptionRepository.findByTenantId(tenantId).orElse(null);
        detail.put("subscription", subscription == null ? null : toSubscriptionView(subscription));
        detail.put("subscriptionHistory", subscriptionEventRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toHistoryView).toList());
        detail.put("salaryDefinitions", salaryPackService.list(tenantId, 0L));
        detail.put("leaveTypes", leaveTypeRepository.findByTenantIdAndStoreId(tenantId,
                stores.isEmpty() ? 0L : stores.get(0).getStoreId()).size());
        detail.put("integrations", toIntegrationView(tenant));
        detail.put("supportSessions", listSupportSessions(tenantId));
        detail.put("health", health(tenant, stores));
        detail.put("readiness", readiness(tenant, stores, subscription));
        return detail;
    }

    @Transactional
    public Map<String, Object> createTenant(Map<String, Object> body) {
        String name = required(body, "tenantName");
        Long tenantId = nextTenantId();
        TenantCompanyMapping tenant = new TenantCompanyMapping();
        tenant.setTenantId(tenantId);
        tenant.setTenantName(name);
        tenant.setApplicationName(stringVal(body.get("applicationName"), name));
        tenant.setSalaryCycleStartDay(asInt(body.get("salaryCycleStartDay"), 1));
        tenant.setSummaryCalculationOffsetDays(asInt(body.get("summaryCalculationOffsetDays"), 0));
        tenant.setSalaryCalculationOffsetDays(asInt(body.get("salaryCalculationOffsetDays"), 0));
        tenant.setIsPaidLeaveApplicable(asBool(body.get("isPaidLeaveApplicable"), true));
        PlatformProduct product = productService.resolve(body);
        tenant.setProductId(product.getId());
        if (!notBlank(stringVal(body.get("applicationName"), null))) {
            tenant.setApplicationName(product.getDisplayName());
        }
        tenant = tenantRepository.save(tenant);

        StoreDetails store = createStoreEntity(tenant, body, false);
        upsertProfile(tenantId, store.getStoreId(), body);
        TenantSubscription subscription = createSubscription(tenantId, body);
        Map<String, Object> result = getTenant(tenantId);
        result.put("firstStoreId", store.getStoreId());
        result.put("subscription", toSubscriptionView(subscription));
        return result;
    }

    @Transactional
    public Map<String, Object> updateTenant(Long tenantId, Map<String, Object> body) {
        TenantCompanyMapping tenant = requireAccessibleTenant(tenantId);
        if (body.containsKey("tenantName") && notBlank(stringVal(body.get("tenantName"), null))) {
            tenant.setTenantName(stringVal(body.get("tenantName"), tenant.getTenantName()));
        }
        if (body.containsKey("applicationName")) {
            tenant.setApplicationName(stringVal(body.get("applicationName"), tenant.getApplicationName()));
        }
        if (body.containsKey("productId") || body.containsKey("productCode")) {
            tenant.setProductId(productService.resolve(body).getId());
        }
        tenantRepository.save(tenant);
        return getTenant(tenantId);
    }

    @Transactional
    public Map<String, Object> addStore(Long tenantId, Map<String, Object> body) {
        TenantCompanyMapping tenant = requireAccessibleTenant(tenantId);
        licenseGuard.assertCanAddStore(tenantId);
        StoreDetails store = createStoreEntity(tenant, body, true);
        upsertProfile(tenantId, store.getStoreId(), body);
        return toStoreView(store);
    }

    @Transactional
    public Map<String, Object> updateStore(Long tenantId, Long storeId, Map<String, Object> body) {
        requireAccessibleTenant(tenantId);
        StoreDetails store = storeRepository.fetchStoreAndTenantDetails(tenantId, storeId)
                .orElseThrow(() -> OpsException.notFound("Store not found."));
        if (body.containsKey("storeName")) store.setStoreName(stringVal(body.get("storeName"), store.getStoreName()));
        if (body.containsKey("timeZone")) store.setTimeZone(stringVal(body.get("timeZone"), store.getTimeZone()));
        if (body.containsKey("storeOpenTime")) store.setStoreOpenTime(stringVal(body.get("storeOpenTime"), store.getStoreOpenTime()));
        if (body.containsKey("storeCloseTime")) store.setStoreCloseTime(stringVal(body.get("storeCloseTime"), store.getStoreCloseTime()));
        if (body.containsKey("financialYearStartMonth")) store.setFinancialYearStartMonth(asInt(body.get("financialYearStartMonth"), store.getFinancialYearStartMonth()));
        if (body.containsKey("currencySymbol")) store.setCurrencySymbol(stringVal(body.get("currencySymbol"), store.getCurrencySymbol()));
        if (body.containsKey("dateFormat")) store.setDateFormat(stringVal(body.get("dateFormat"), store.getDateFormat()));
        if (body.containsKey("isActualTimeBasedAttendance")) store.setIsActualTimeBasedAttendance(asBool(body.get("isActualTimeBasedAttendance"), false));
        if (body.containsKey("externalBranchId")) store.setExternalBranchId(stringVal(body.get("externalBranchId"), null));
        if (body.get("payrollLockedUpToDate") != null && !String.valueOf(body.get("payrollLockedUpToDate")).isBlank()) {
            store.setPayrollLockedUpToDate(LocalDate.parse(String.valueOf(body.get("payrollLockedUpToDate"))));
        }
        storeRepository.save(store);
        upsertProfile(tenantId, storeId, body);
        return toStoreView(store);
    }

    @Transactional
    public Map<String, Object> createAdmin(Long tenantId, Map<String, Object> body) {
        TenantCompanyMapping tenant = requireAccessibleTenant(tenantId);
        Long storeId = asLong(body.get("storeId"), firstStoreId(tenantId));
        if (storeId == null) {
            throw OpsException.badRequest("Create a store before the admin user.");
        }
        licenseGuard.assertCanAddStaff(tenantId);
        String username = required(body, "username");
        String password = required(body, "password");
        if (password.length() < 8) {
            throw OpsException.badRequest("Password must be at least 8 characters.");
        }
        if (isReservedUsername(username)) {
            throw OpsException.badRequest("That username is reserved for platform users.");
        }
        if (personnelRepository.findByUsername(username).isPresent()
                || platformUserRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw OpsException.conflict("Username is already taken.");
        }
        TenantSubscription subscription = subscriptionRepository.findByTenantId(tenantId).orElse(null);
        boolean hospitality = subscription != null && Boolean.TRUE.equals(subscription.getModuleHospitality());

        Role admin = ensureRole(tenantId, "Admin", hospitality ? ADMIN_PERMISSIONS + "," + HOSPITALITY_PERMISSIONS : ADMIN_PERMISSIONS);
        ensureRole(tenantId, "Manager", String.join(",",
                Permissions.VIEW_OTHER_STAFF, Permissions.MANAGE_SHIFTS, Permissions.MANAGE_ATTENDANCE, Permissions.MANAGE_LEAVES));
        ensureRole(tenantId, "Staff", "");

        PersonnelDetails adminUser = new PersonnelDetails();
        adminUser.setFirstName(stringVal(body.get("firstName"), "Admin"));
        adminUser.setLastName(stringVal(body.get("lastName"), tenant.getTenantName()));
        adminUser.setUsername(username);
        adminUser.setPassword(passwordEncoder.encode(password));
        adminUser.setEmail(stringVal(body.get("email"), username + "@" + tenantId + ".local"));
        adminUser.setApplicationTenantId(tenantId);
        adminUser.setStoreId(storeId);
        adminUser.setTenantCompanyMappingId(tenant.getId());
        adminUser.setApplicationName(tenant.getApplicationName());
        adminUser.setEmployeeCode(stringVal(body.get("employeeCode"), "ADM-" + tenantId));
        adminUser.setActive(true);
        adminUser.setDesignation("Administrator");
        adminUser = personnelRepository.save(adminUser);

        StoreStaffRole assignment = new StoreStaffRole();
        assignment.setTenantId(tenantId);
        assignment.setStoreId(storeId);
        assignment.setStaffId(adminUser.getId());
        assignment.setRoleId(admin.getId());
        assignment.setActive(1);
        assignment.setSystemCreatedOn(LocalDateTime.now());
        storeStaffRoleRepository.save(assignment);

        Map<String, Object> view = toStaffView(adminUser);
        view.put("temporaryPassword", password);
        view.put("roleName", admin.getName());
        return view;
    }

    @Transactional
    public Map<String, Object> applyLeavePack(Long tenantId, Long storeId) {
        requireAccessibleTenant(tenantId);
        Long resolvedStore = storeId == null ? firstStoreId(tenantId) : storeId;
        if (resolvedStore == null) {
            throw OpsException.badRequest("Create a store before applying a leave pack.");
        }
        if (!leaveTypeRepository.findByTenantIdAndStoreId(tenantId, resolvedStore).isEmpty()) {
            return Map.of("applied", false, "message", "Leave types already exist.");
        }
        LeaveType cl = saveType(tenantId, resolvedStore, "CL", "Casual Leave", true);
        LeaveType sl = saveType(tenantId, resolvedStore, "SL", "Sick Leave", true);
        LeaveType el = saveType(tenantId, resolvedStore, "EL", "Earned Leave", true);
        saveType(tenantId, resolvedStore, "OH", "Optional Holiday", true);

        LeavePlan plan = LeavePlan.builder()
                .tenantId(tenantId)
                .storeId(resolvedStore)
                .locationId(resolvedStore)
                .planName("Default plan")
                .effectiveYear(LocalDate.now().getYear())
                .build();
        plan = leavePlanRepository.save(plan);
        saveRule(plan, cl, new BigDecimal("12"), LeavePlanRule.AccrualFrequency.ANNUALLY);
        saveRule(plan, sl, new BigDecimal("8"), LeavePlanRule.AccrualFrequency.ANNUALLY);
        saveRule(plan, el, new BigDecimal("15"), LeavePlanRule.AccrualFrequency.ANNUALLY);
        return Map.of("applied", true, "planId", plan.getId(), "planName", plan.getPlanName());
    }

    @Transactional
    public Map<String, Object> applyDefaultShift(Long tenantId, Long storeId) {
        requireAccessibleTenant(tenantId);
        Long resolvedStore = storeId == null ? firstStoreId(tenantId) : storeId;
        StoreDetails store = storeRepository.fetchStoreAndTenantDetails(tenantId, resolvedStore)
                .orElseThrow(() -> OpsException.notFound("Store not found."));
        if (!shiftSlotsRepository.findByTenantIdAndStoreId(tenantId, resolvedStore).isEmpty()) {
            return Map.of("applied", false, "message", "Shift slots already exist.");
        }
        SShiftsSlots slot = new SShiftsSlots();
        slot.setTenantId(tenantId);
        slot.setStoreId(resolvedStore);
        slot.setShiftName("General");
        slot.setStartTime(store.getStoreOpenTime() == null ? "09:00" : store.getStoreOpenTime());
        slot.setEndTime(store.getStoreCloseTime() == null ? "18:00" : store.getStoreCloseTime());
        slot.setActive(true);
        slot.setOvernight(false);
        shiftSlotsRepository.save(slot);
        return Map.of("applied", true, "shiftName", slot.getShiftName());
    }

    @Transactional
    public Map<String, Object> updateIntegrations(Long tenantId, Map<String, Object> body) {
        TenantCompanyMapping tenant = requireAccessibleTenant(tenantId);
        if (body.containsKey("biometricVendorName")) tenant.setBiometricVendorName(stringVal(body.get("biometricVendorName"), null));
        if (body.containsKey("vendorUrl")) tenant.setVendorUrl(stringVal(body.get("vendorUrl"), null));
        if (body.containsKey("vendorCorporateId")) tenant.setVendorCorporateId(stringVal(body.get("vendorCorporateId"), null));
        if (body.containsKey("vendorUserName")) tenant.setVendorUserName(stringVal(body.get("vendorUserName"), null));
        if (body.containsKey("vendorPassword") && body.get("vendorPassword") != null
                && !String.valueOf(body.get("vendorPassword")).isBlank()) {
            tenant.setVendorPassword(String.valueOf(body.get("vendorPassword")));
        }
        if (body.containsKey("externalSoftwareUrl")) tenant.setExternalSoftwareUrl(stringVal(body.get("externalSoftwareUrl"), null));
        if (body.containsKey("externalAppName")) tenant.setExternalAppName(stringVal(body.get("externalAppName"), null));
        tenantRepository.save(tenant);

        Long storeId = asLong(body.get("storeId"), firstStoreId(tenantId));
        if (storeId != null) {
            StoreDetails store = storeRepository.fetchStoreAndTenantDetails(tenantId, storeId)
                    .orElseThrow(() -> OpsException.notFound("Store not found."));
            if (body.containsKey("externalBranchId")) {
                store.setExternalBranchId(stringVal(body.get("externalBranchId"), null));
            }
            if (body.get("terminals") instanceof List<?> terminals) {
                List<TerminalDetails> details = new ArrayList<>();
                for (Object raw : terminals) {
                    if (raw == null || String.valueOf(raw).isBlank()) {
                        continue;
                    }
                    TerminalDetails terminal = new TerminalDetails();
                    terminal.setTerminalSerialNumber(String.valueOf(raw).trim());
                    details.add(terminal);
                }
                store.setTerminalDetailsList(details);
            }
            storeRepository.save(store);
            boolean schedule = asBool(body.get("createAttendanceScheduler"),
                    tenant.getBiometricVendorName() != null && !tenant.getBiometricVendorName().isBlank());
            if (schedule) {
                PayrollSchedulerInfo info = new PayrollSchedulerInfo();
                info.setTenantId(tenantId);
                info.setStoreId(storeId);
                info.setEvent("ATTENDANCEDATAREMINDER");
                info.setInvocationTime(Instant.now());
                info.setProcessed(null);
                schedulerInfoRepo.save(info);
            }
        }
        return Map.of("updated", true);
    }

    @Transactional
    public Map<String, Object> persistStatutory(Long tenantId, Long storeId) {
        requireAccessibleTenant(tenantId);
        Long resolvedStore = storeId == null ? firstStoreId(tenantId) : storeId;
        StatutoryConfig config = statutoryConfigRepository.findByTenantIdAndStoreId(tenantId, resolvedStore)
                .orElseGet(StatutoryConfig::new);
        config.setTenantId(tenantId);
        config.setStoreId(resolvedStore);
        statutoryConfigRepository.save(config);
        return Map.of("saved", true);
    }

    @Transactional
    public Map<String, Object> updateSubscription(Long tenantId, Map<String, Object> body) {
        requireAccessibleTenant(tenantId);
        TenantSubscription subscription = licenseGuard.require(tenantId);
        TenantSubscription.Status oldStatus = subscription.getStatus();
        LocalDate oldEnd = subscription.getEndsOn();
        applySubscriptionFields(subscription, body);
        TenantSubscription.Status requested = parseStatus(body.get("status"), subscription.getStatus());
        if ((requested == TenantSubscription.Status.SUSPENDED || requested == TenantSubscription.Status.EXPIRED)
                && !SecurityUtils.hasAuthority(Permissions.SUPERADMIN)) {
            throw OpsException.forbidden("Only superadmin can suspend or expire a license.");
        }
        subscription.setStatus(requested);
        subscriptionRepository.save(subscription);
        TenantSubscriptionEvent event = new TenantSubscriptionEvent();
        event.setTenantId(tenantId);
        event.setAction("UPDATE");
        event.setActorUsername(SecurityUtils.currentUsername());
        event.setOldStatus(oldStatus.name());
        event.setNewStatus(subscription.getStatus().name());
        event.setOldEndsOn(oldEnd);
        event.setNewEndsOn(subscription.getEndsOn());
        event.setNotes(stringVal(body.get("notes"), null));
        event.setCreatedAt(Instant.now());
        subscriptionEventRepository.save(event);
        return toSubscriptionView(subscription);
    }

    @Transactional
    public Map<String, Object> cloneFrom(Long targetTenantId, Long sourceTenantId) {
        requireAccessibleTenant(targetTenantId);
        requireAccessibleTenant(sourceTenantId);
        salaryPackService.cloneFrom(sourceTenantId, targetTenantId);
        Long sourceStore = firstStoreId(sourceTenantId);
        Long targetStore = firstStoreId(targetTenantId);
        if (sourceStore != null && targetStore != null
                && leaveTypeRepository.findByTenantIdAndStoreId(targetTenantId, targetStore).isEmpty()) {
            applyLeavePack(targetTenantId, targetStore);
        }
        StoreProfileConfig sourceProfile = sourceStore == null ? null
                : profileRepository.findByTenantIdAndStoreId(sourceTenantId, sourceStore).orElse(null);
        StoreProfileConfig targetProfile = targetStore == null ? null
                : profileRepository.findByTenantIdAndStoreId(targetTenantId, targetStore).orElse(null);
        if (sourceProfile != null && targetStore != null) {
            Map<String, Object> profile = new LinkedHashMap<>();
            if (targetProfile == null || !notBlank(targetProfile.getCompanyName())) {
                profile.put("companyName", sourceProfile.getCompanyName());
            }
            if (targetProfile == null || !notBlank(targetProfile.getAddress())) {
                profile.put("address", sourceProfile.getAddress());
            }
            if (targetProfile == null || targetProfile.getWorkingDaysPerMonth() == null) {
                profile.put("workingDaysPerMonth", sourceProfile.getWorkingDaysPerMonth());
            }
            if (targetProfile == null || targetProfile.getWorkingHoursPerDay() == null) {
                profile.put("workingHoursPerDay", sourceProfile.getWorkingHoursPerDay());
            }
            if (!profile.isEmpty()) {
                upsertProfile(targetTenantId, targetStore, profile);
            }
        }
        cloneHolidays(sourceTenantId, sourceStore, targetTenantId, targetStore);
        return getTenant(targetTenantId);
    }

    public Map<String, Object> environment() {
        Map<String, Object> env = new LinkedHashMap<>();
        env.put("rabbitConfigured", notBlank(System.getenv("RABBITMQ_HOST")) || notBlank(System.getenv("RABBITMQ_USERNAME")));
        env.put("mailConfigured", notBlank(System.getenv("MAIL_USERNAME")));
        return env;
    }

    @Transactional
    public Map<String, Object> applyHolidayPack(Long tenantId, Long storeId, Integer year) {
        requireAccessibleTenant(tenantId);
        List<Long> storeIds = new ArrayList<>();
        if (storeId != null) {
            storeIds.add(storeId);
        } else {
            storeIds.addAll(storeRepository.fetchStoreDetailListForTenant(tenantId, 0L)
                    .stream().map(StoreDetails::getStoreId).toList());
        }
        if (storeIds.isEmpty()) {
            throw OpsException.badRequest("Create a store before applying a holiday pack.");
        }
        int resolvedYear = year == null ? LocalDate.now().getYear() : year;
        int created = 0;
        for (Long resolvedStore : storeIds) {
            for (Map.Entry<LocalDate, String> holiday : nationalHolidays(resolvedYear)) {
                if (holidayRepository.existsByTenantIdAndStoreIdAndHolidayDate(tenantId, resolvedStore, holiday.getKey())) {
                    continue;
                }
                StoreHoliday row = new StoreHoliday();
                row.setTenantId(tenantId);
                row.setStoreId(resolvedStore);
                row.setHolidayDate(holiday.getKey());
                row.setHolidayName(holiday.getValue());
                row.setIsOptional(false);
                holidayRepository.save(row);
                created++;
            }
        }
        return Map.of("applied", created > 0, "created", created, "year", resolvedYear, "storeIds", storeIds);
    }

    @Transactional
    public Map<String, Object> resetAdminPassword(Long tenantId, Long staffId, Map<String, Object> body) {
        PersonnelDetails person = requireTenantStaff(tenantId, staffId);
        String password = required(body, "password");
        if (password.length() < 8) {
            throw OpsException.badRequest("Password must be at least 8 characters.");
        }
        person.setPassword(passwordEncoder.encode(password));
        personnelRepository.save(person);
        Map<String, Object> view = toStaffView(person);
        view.put("temporaryPassword", password);
        return view;
    }

    @Transactional
    public Map<String, Object> setAdminActive(Long tenantId, Long staffId, boolean active) {
        PersonnelDetails person = requireTenantStaff(tenantId, staffId);
        person.setActive(active);
        personnelRepository.save(person);
        return toStaffView(person);
    }

    @Transactional
    public Map<String, Object> createSupportSession(Long tenantId, Long storeId, Long staffId) {
        if (!SecurityUtils.hasAuthority(Permissions.SUPERADMIN)) {
            throw OpsException.forbidden("Only superadmin can mint a support session.");
        }
        PersonnelDetails personnel = requireTenantStaff(tenantId, staffId);
        if (!storeId.equals(personnel.getStoreId())) {
            throw OpsException.badRequest("Staff does not belong to that store.");
        }
        userDetailsService.loadUserByUsername(personnel.getUsername());
        long supportTtlMs = 30 * 60 * 1000L;
        String tokenId = UUID.randomUUID().toString();
        String handoffCode = HexFormat.of().formatHex(randomBytes(24));
        Instant now = Instant.now();
        SupportSession session = new SupportSession();
        session.setTenantId(tenantId);
        session.setStoreId(storeId);
        session.setStaffId(staffId);
        session.setActorUsername(SecurityUtils.currentUsername());
        session.setTokenId(tokenId);
        session.setHandoffCode(handoffCode);
        session.setExpiresAt(now.plusMillis(supportTtlMs));
        session.setHandoffExpiresAt(now.plusSeconds(120));
        session.setCreatedAt(now);
        supportSessionRepository.save(session);
        Map<String, Object> view = toSupportSessionView(session, personnel.getUsername());
        view.put("launchPath", "/ops/support-launch?code=" + handoffCode);
        view.put("expiresInMs", supportTtlMs);
        return view;
    }

    @Transactional
    public Map<String, Object> consumeSupportHandoff(String code) {
        if (code == null || code.isBlank()) {
            throw OpsException.badRequest("Support handoff code is required.");
        }
        SupportSession session = supportSessionRepository.findByHandoffCode(code.trim())
                .orElseThrow(() -> OpsException.notFound("Support session is not available."));
        Instant now = Instant.now();
        if (session.getHandoffConsumedAt() != null
                || session.getRevokedAt() != null
                || session.getHandoffExpiresAt() == null
                || !now.isBefore(session.getHandoffExpiresAt())
                || !session.isActive(now)) {
            throw OpsException.badRequest("That support link has expired or was already used.");
        }
        PersonnelDetails personnel = personnelRepository.findById(session.getStaffId())
                .orElseThrow(() -> OpsException.notFound("Staff not found."));
        CustomUserDetails details = (CustomUserDetails) userDetailsService.loadUserByUsername(personnel.getUsername());
        long remainingMs = Math.max(60_000L, session.getExpiresAt().toEpochMilli() - now.toEpochMilli());
        String token = jwtUtil.generateToken(details, remainingMs, Map.of(
                "supportSessionId", session.getTokenId(),
                "supportSession", true
        ));
        session.setHandoffConsumedAt(now);
        supportSessionRepository.save(session);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("success", true);
        view.put("token", token);
        view.put("principalType", CustomUserDetails.PRINCIPAL_CLIENT);
        view.put("supportSession", true);
        view.put("supportSessionId", session.getId());
        view.put("username", personnel.getUsername());
        view.put("firstName", personnel.getFirstName());
        view.put("lastName", personnel.getLastName());
        view.put("email", personnel.getEmail());
        view.put("designation", personnel.getDesignation());
        view.put("staffId", personnel.getId());
        view.put("tenantId", personnel.getApplicationTenantId());
        view.put("storeId", personnel.getStoreId());
        view.put("active", personnel.getActive());
        view.put("roles", details.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .toList());
        view.put("expiresAt", session.getExpiresAt().toString());
        return view;
    }

    @Transactional
    public Map<String, Object> revokeSupportSession(Long tenantId, Long sessionId) {
        requireAccessibleTenant(tenantId);
        SupportSession session = supportSessionRepository.findById(sessionId)
                .orElseThrow(() -> OpsException.notFound("Support session not found."));
        if (!tenantId.equals(session.getTenantId())) {
            throw OpsException.forbidden("Support session belongs to another tenant.");
        }
        session.setRevokedAt(Instant.now());
        supportSessionRepository.save(session);
        return Map.of("revoked", true, "sessionId", sessionId);
    }

    public List<Map<String, Object>> listSupportSessions(Long tenantId) {
        Instant now = Instant.now();
        return supportSessionRepository.findByTenantIdAndExpiresAtAfterOrderByCreatedAtDesc(tenantId, now)
                .stream()
                .map((session) -> {
                    String username = personnelRepository.findById(session.getStaffId())
                            .map(PersonnelDetails::getUsername)
                            .orElse(null);
                    return toSupportSessionView(session, username);
                })
                .toList();
    }

    private TenantCompanyMapping requireTenant(Long tenantId) {
        return tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> OpsException.notFound("Tenant not found."));
    }

    private TenantCompanyMapping requireAccessibleTenant(Long tenantId) {
        TenantCompanyMapping tenant = requireTenant(tenantId);
        if (SecurityUtils.hasAuthority(Permissions.SUPERADMIN)) {
            return tenant;
        }
        TenantSubscription subscription = subscriptionRepository.findByTenantId(tenantId).orElse(null);
        Long ownerId = SecurityUtils.currentPlatformUserId();
        if (subscription == null || ownerId == null || !ownerId.equals(subscription.getSalesOwnerId())) {
            throw OpsException.forbidden("That client is not in your book.");
        }
        return tenant;
    }

    private PersonnelDetails requireTenantStaff(Long tenantId, Long staffId) {
        requireAccessibleTenant(tenantId);
        PersonnelDetails person = personnelRepository.findById(staffId)
                .orElseThrow(() -> OpsException.notFound("Staff not found."));
        if (!tenantId.equals(person.getApplicationTenantId())) {
            throw OpsException.forbidden("Staff does not belong to that tenant.");
        }
        return person;
    }

    private StoreDetails createStoreEntity(TenantCompanyMapping tenant, Map<String, Object> body, boolean extra) {
        StoreDetails store = new StoreDetails();
        store.setTenantCompanyMapping(tenant);
        store.setStoreId(nextStoreId());
        store.setStoreName(stringVal(body.get("storeName"), extra ? "Store " + store.getStoreId() : "Main store"));
        store.setTimeZone(stringVal(body.get("timeZone"), "Asia/Kolkata"));
        store.setStoreOpenTime(stringVal(body.get("storeOpenTime"), "09:00"));
        store.setStoreCloseTime(stringVal(body.get("storeCloseTime"), "18:00"));
        store.setFinancialYearStartMonth(asInt(body.get("financialYearStartMonth"), 4));
        store.setCurrencySymbol(stringVal(body.get("currencySymbol"), "₹"));
        store.setDateFormat(stringVal(body.get("dateFormat"), "dd-MM-yyyy"));
        store.setIsActualTimeBasedAttendance(asBool(body.get("isActualTimeBasedAttendance"), false));
        store.setExternalBranchId(stringVal(body.get("externalBranchId"), null));
        if (body.get("payrollLockedUpToDate") != null && !String.valueOf(body.get("payrollLockedUpToDate")).isBlank()) {
            store.setPayrollLockedUpToDate(LocalDate.parse(String.valueOf(body.get("payrollLockedUpToDate"))));
        }
        return storeRepository.save(store);
    }

    private void upsertProfile(Long tenantId, Long storeId, Map<String, Object> body) {
        StoreProfileConfig profile = profileRepository.findByTenantIdAndStoreId(tenantId, storeId)
                .orElseGet(StoreProfileConfig::new);
        profile.setTenantId(tenantId);
        profile.setStoreId(storeId);
        if (body.containsKey("companyName") || profile.getCompanyName() == null) {
            profile.setCompanyName(stringVal(body.get("companyName"), stringVal(body.get("tenantName"), profile.getCompanyName())));
        }
        if (body.containsKey("address")) profile.setAddress(stringVal(body.get("address"), profile.getAddress()));
        if (body.containsKey("email")) profile.setEmail(stringVal(body.get("email"), profile.getEmail()));
        if (body.containsKey("phone")) profile.setPhone(stringVal(body.get("phone"), profile.getPhone()));
        if (body.containsKey("gstNo")) profile.setGstNo(stringVal(body.get("gstNo"), profile.getGstNo()));
        if (body.get("workingDaysPerMonth") != null) {
            profile.setWorkingDaysPerMonth(asDouble(body.get("workingDaysPerMonth"), 30D));
        }
        if (body.get("workingHoursPerDay") != null) {
            profile.setWorkingHoursPerDay(asDouble(body.get("workingHoursPerDay"), 9.5));
        }
        profileRepository.save(profile);
    }

    private TenantSubscription createSubscription(Long tenantId, Map<String, Object> body) {
        TenantSubscription subscription = new TenantSubscription();
        subscription.setTenantId(tenantId);
        applySubscriptionFields(subscription, body);
        if (subscription.getStatus() == null) {
            subscription.setStatus(TenantSubscription.Status.DRAFT);
        }
        if (subscription.getPlanCode() == TenantSubscription.PlanCode.HOSPITALITY) {
            subscription.setModuleHospitality(true);
        }
        if (subscription.getSalesOwnerId() == null) {
            subscription.setSalesOwnerId(SecurityUtils.currentPlatformUserId());
        }
        subscription = subscriptionRepository.save(subscription);
        TenantSubscriptionEvent event = new TenantSubscriptionEvent();
        event.setTenantId(tenantId);
        event.setAction("CREATE");
        event.setActorUsername(SecurityUtils.currentUsername());
        event.setNewStatus(subscription.getStatus().name());
        event.setNewEndsOn(subscription.getEndsOn());
        event.setCreatedAt(Instant.now());
        subscriptionEventRepository.save(event);
        return subscription;
    }

    private void applySubscriptionFields(TenantSubscription subscription, Map<String, Object> body) {
        if (body.get("planCode") != null) {
            subscription.setPlanCode(TenantSubscription.PlanCode.valueOf(String.valueOf(body.get("planCode")).toUpperCase()));
            if (subscription.getPlanCode() == TenantSubscription.PlanCode.HOSPITALITY
                    && !body.containsKey("moduleHospitality")) {
                subscription.setModuleHospitality(true);
            }
        }
        if (body.get("status") != null) {
            subscription.setStatus(parseStatus(body.get("status"), subscription.getStatus()));
        }
        if (body.get("startsOn") != null && !String.valueOf(body.get("startsOn")).isBlank()) {
            subscription.setStartsOn(LocalDate.parse(String.valueOf(body.get("startsOn"))));
        }
        if (body.get("endsOn") != null && !String.valueOf(body.get("endsOn")).isBlank()) {
            subscription.setEndsOn(LocalDate.parse(String.valueOf(body.get("endsOn"))));
        }
        if (body.containsKey("maxStores")) subscription.setMaxStores(asInt(body.get("maxStores"), null));
        if (body.containsKey("maxStaff")) subscription.setMaxStaff(asInt(body.get("maxStaff"), null));
        if (body.containsKey("moduleSalary")) subscription.setModuleSalary(asBool(body.get("moduleSalary"), true));
        if (body.containsKey("moduleLeave")) subscription.setModuleLeave(asBool(body.get("moduleLeave"), true));
        if (body.containsKey("moduleShifts")) subscription.setModuleShifts(asBool(body.get("moduleShifts"), true));
        if (body.containsKey("moduleReports")) subscription.setModuleReports(asBool(body.get("moduleReports"), true));
        if (body.containsKey("moduleHospitality")) subscription.setModuleHospitality(asBool(body.get("moduleHospitality"), false));
        if (body.containsKey("moduleBiometric")) subscription.setModuleBiometric(asBool(body.get("moduleBiometric"), false));
        if (body.containsKey("moduleStaffSync")) subscription.setModuleStaffSync(asBool(body.get("moduleStaffSync"), false));
        if (body.containsKey("notes")) subscription.setNotes(stringVal(body.get("notes"), null));
        if (body.containsKey("salesOwnerId")) {
            if (!SecurityUtils.hasAuthority(Permissions.SUPERADMIN)) {
                if (asLong(body.get("salesOwnerId"), null) != null
                        && !java.util.Objects.equals(asLong(body.get("salesOwnerId"), null), SecurityUtils.currentPlatformUserId())) {
                    throw OpsException.forbidden("Only superadmin can assign a salesperson.");
                }
            } else {
                Long ownerId = asLong(body.get("salesOwnerId"), null);
                if (ownerId != null && platformUserRepository.findById(ownerId).isEmpty()) {
                    throw OpsException.badRequest("Salesperson is not a platform user.");
                }
                subscription.setSalesOwnerId(ownerId);
            }
        }
    }

    private TenantSubscription.Status parseStatus(Object raw, TenantSubscription.Status fallback) {
        if (raw == null || String.valueOf(raw).isBlank()) {
            return fallback;
        }
        return TenantSubscription.Status.valueOf(String.valueOf(raw).trim().toUpperCase());
    }

    private Role ensureRole(Long tenantId, String name, String permissions) {
        Role existing = roleRepository.findByNameAndTenantId(name, tenantId);
        if (existing != null) {
            return existing;
        }
        Role role = new Role();
        role.setName(name);
        role.setTenantId(tenantId);
        role.setActive(1);
        role.setHideFromUi(false);
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    private LeaveType saveType(Long tenantId, Long storeId, String code, String name, boolean paid) {
        LeaveType type = LeaveType.builder()
                .tenantId(tenantId)
                .storeId(storeId)
                .locationId(storeId)
                .leaveCode(code)
                .leaveName(name)
                .paid(paid)
                .applicableGender("ALL")
                .build();
        return leaveTypeRepository.save(type);
    }

    private void saveRule(LeavePlan plan, LeaveType type, BigDecimal allotment, LeavePlanRule.AccrualFrequency frequency) {
        LeavePlanRule rule = LeavePlanRule.builder()
                .leavePlan(plan)
                .leaveType(type)
                .annualAllotment(allotment)
                .allowNegativeBalance(false)
                .accrualFrequency(frequency)
                .build();
        leavePlanRuleRepository.save(rule);
    }

    private Map<String, Object> readiness(TenantCompanyMapping tenant, List<StoreDetails> stores, TenantSubscription subscription) {
        Long storeId = stores.isEmpty() ? null : stores.get(0).getStoreId();
        StoreProfileConfig profile = storeId == null ? null
                : profileRepository.findByTenantIdAndStoreId(tenant.getTenantId(), storeId).orElse(null);
        boolean letterhead = profile != null && notBlank(profile.getCompanyName()) && notBlank(profile.getAddress());
        boolean holidays = storeId != null && !holidayRepository.findByTenantIdAndStoreId(tenant.getTenantId(), storeId).isEmpty();
        boolean lockDate = stores.stream().anyMatch((store) -> store.getPayrollLockedUpToDate() != null);
        boolean attendanceSource = notBlank(tenant.getVendorUrl()) || notBlank(tenant.getBiometricVendorName());
        Map<String, Object> ready = new LinkedHashMap<>();
        ready.put("tenant", tenant != null);
        ready.put("store", !stores.isEmpty());
        ready.put("subscription", subscription != null);
        List<PersonnelDetails> people = personnelRepository.findByApplicationTenantId(tenant.getTenantId());
        ready.put("adminUser", !people.isEmpty());
        ready.put("firstAdminSignedIn", people.stream().anyMatch((person) -> person.getLastLoginAt() != null));
        ready.put("salaryDefinitions", !salaryDefinitionsRepository.findAllByTenantIdAndStoreId(tenant.getTenantId(), 0L).isEmpty()
                || (storeId != null && !salaryDefinitionsRepository.findAllByTenantIdAndStoreId(tenant.getTenantId(), storeId).isEmpty()));
        ready.put("leavePack", storeId != null && !leaveTypeRepository.findByTenantIdAndStoreId(tenant.getTenantId(), storeId).isEmpty());
        ready.put("storeProfile", profile != null);
        ready.put("letterhead", letterhead);
        ready.put("holidays", holidays);
        ready.put("lockDate", lockDate);
        ready.put("attendanceSource", attendanceSource);
        ready.put("canClientLogin", subscription != null && subscription.allowsClientLogin());
        return ready;
    }

    private Map<String, Object> toListItem(TenantCompanyMapping tenant) {
        List<StoreDetails> stores = storeRepository.fetchStoreDetailListForTenant(tenant.getTenantId(), 0L);
        TenantSubscription subscription = subscriptionRepository.findByTenantId(tenant.getTenantId()).orElse(null);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("tenantId", tenant.getTenantId());
        item.put("tenantName", tenant.getTenantName());
        item.put("applicationName", tenant.getApplicationName());
        item.put("product", productService.toView(productService.resolveId(tenant.getProductId())));
        item.put("storeCount", stores.size());
        item.put("staffCount", personnelRepository.countByApplicationTenantId(tenant.getTenantId()));
        item.put("subscription", subscription == null ? null : toSubscriptionView(subscription));
        return item;
    }

    private Map<String, Object> toStoreView(StoreDetails store) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", store.getId());
        view.put("storeId", store.getStoreId());
        view.put("storeName", store.getStoreName());
        view.put("timeZone", store.getTimeZone());
        view.put("storeOpenTime", store.getStoreOpenTime());
        view.put("storeCloseTime", store.getStoreCloseTime());
        view.put("financialYearStartMonth", store.getFinancialYearStartMonth());
        view.put("payrollLockedUpToDate", store.getPayrollLockedUpToDate());
        view.put("currencySymbol", store.getCurrencySymbol());
        view.put("dateFormat", store.getDateFormat());
        view.put("isActualTimeBasedAttendance", store.getIsActualTimeBasedAttendance());
        view.put("externalBranchId", store.getExternalBranchId());
        view.put("recentSummaryCalculatedMonth", store.getRecentSummaryCalculatedMonth());
        Long tenantId = store.getTenantCompanyMapping() != null
                ? store.getTenantCompanyMapping().getTenantId()
                : null;
        if (tenantId != null) {
            profileRepository.findByTenantIdAndStoreId(tenantId, store.getStoreId()).ifPresent((profile) -> {
                view.put("companyName", profile.getCompanyName());
                view.put("address", profile.getAddress());
                view.put("email", profile.getEmail());
                view.put("phone", profile.getPhone());
                view.put("gstNo", profile.getGstNo());
                view.put("workingDaysPerMonth", profile.getWorkingDaysPerMonth());
                view.put("workingHoursPerDay", profile.getWorkingHoursPerDay());
                view.put("logoPath", profile.getLogoPath());
            });
            view.put("holidayCount", holidayRepository.findByTenantIdAndStoreId(tenantId, store.getStoreId()).size());
        }
        return view;
    }

    private Map<String, Object> toStaffView(PersonnelDetails personnel) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", personnel.getId());
        view.put("username", personnel.getUsername());
        view.put("email", personnel.getEmail());
        view.put("firstName", personnel.getFirstName());
        view.put("lastName", personnel.getLastName());
        view.put("storeId", personnel.getStoreId());
        view.put("active", personnel.getActive());
        view.put("lastLoginAt", personnel.getLastLoginAt() == null ? null : personnel.getLastLoginAt().toString());
        view.put("hasSignedIn", personnel.getLastLoginAt() != null);
        return view;
    }

    private Map<String, Object> toSubscriptionView(TenantSubscription subscription) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("tenantId", subscription.getTenantId());
        view.put("status", licenseGuard.effectiveStatus(subscription).name());
        view.put("storedStatus", subscription.getStatus().name());
        view.put("planCode", subscription.getPlanCode().name());
        view.put("startsOn", subscription.getStartsOn());
        view.put("endsOn", subscription.getEndsOn());
        view.put("maxStores", subscription.getMaxStores());
        view.put("maxStaff", subscription.getMaxStaff());
        view.put("moduleSalary", subscription.getModuleSalary());
        view.put("moduleLeave", subscription.getModuleLeave());
        view.put("moduleShifts", subscription.getModuleShifts());
        view.put("moduleReports", subscription.getModuleReports());
        view.put("moduleHospitality", subscription.getModuleHospitality());
        view.put("moduleBiometric", subscription.getModuleBiometric());
        view.put("moduleStaffSync", subscription.getModuleStaffSync());
        view.put("notes", subscription.getNotes());
        view.put("salesOwnerId", subscription.getSalesOwnerId());
        if (subscription.getSalesOwnerId() != null) {
            platformUserRepository.findById(subscription.getSalesOwnerId()).ifPresent((owner) -> {
                view.put("salesOwnerUsername", owner.getUsername());
                view.put("salesOwnerName", owner.getDisplayName() != null && !owner.getDisplayName().isBlank()
                        ? owner.getDisplayName()
                        : owner.getUsername());
            });
        }
        view.put("allowsClientLogin", subscription.allowsClientLogin());
        return view;
    }

    private Long firstStoreId(Long tenantId) {
        List<StoreDetails> stores = storeRepository.fetchStoreDetailListForTenant(tenantId, 0L);
        return stores.isEmpty() ? null : stores.get(0).getStoreId();
    }

    private Long nextTenantId() {
        Long max = tenantRepository.findMaxTenantId();
        return (max == null ? 0L : max) + 1L;
    }

    private Long nextStoreId() {
        Long max = storeRepository.findMaxStoreId();
        return (max == null ? 0L : max) + 1L;
    }

    private String required(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw OpsException.badRequest(key + " is required.");
        }
        return String.valueOf(value).trim();
    }

    private String stringVal(Object value, String fallback) {
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return String.valueOf(value).trim();
    }

    private Integer asInt(Object value, Integer fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Long asLong(Object value, Long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Double asDouble(Object value, Double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    private Boolean asBool(Object value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isReservedUsername(String username) {
        String normalized = username.trim().toLowerCase();
        return "superadmin".equals(normalized) || "sales".equals(normalized);
    }

    private boolean ownsSubscription(Object subscription, Long ownerId) {
        if (!(subscription instanceof Map<?, ?> map) || ownerId == null) {
            return false;
        }
        return ownerId.equals(asLong(map.get("salesOwnerId"), null));
    }

    private Map<String, Object> toHistoryView(TenantSubscriptionEvent event) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", event.getId());
        view.put("action", event.getAction());
        view.put("actorUsername", event.getActorUsername());
        view.put("oldStatus", event.getOldStatus());
        view.put("newStatus", event.getNewStatus());
        view.put("oldEndsOn", event.getOldEndsOn());
        view.put("newEndsOn", event.getNewEndsOn());
        view.put("notes", event.getNotes());
        view.put("createdAt", event.getCreatedAt() == null ? null : event.getCreatedAt().toString());
        return view;
    }

    private Map<String, Object> toIntegrationView(TenantCompanyMapping tenant) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("biometricVendorName", tenant.getBiometricVendorName());
        view.put("vendorUrl", tenant.getVendorUrl());
        view.put("vendorCorporateId", tenant.getVendorCorporateId());
        view.put("vendorUserName", tenant.getVendorUserName());
        view.put("vendorPasswordSet", notBlank(tenant.getVendorPassword()));
        view.put("externalSoftwareUrl", tenant.getExternalSoftwareUrl());
        view.put("externalAppName", tenant.getExternalAppName());
        return view;
    }

    private Map<String, Object> health(TenantCompanyMapping tenant, List<StoreDetails> stores) {
        Map<String, Object> env = environment();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("rabbitConfigured", env.get("rabbitConfigured"));
        view.put("mailConfigured", env.get("mailConfigured"));
        view.put("biometricVendorName", tenant.getBiometricVendorName());
        view.put("vendorUrl", tenant.getVendorUrl());
        List<PayrollSchedulerInfo> reminders = schedulerInfoRepo.findByTenantIdOrderByInvocationTimeDesc(tenant.getTenantId());
        if (!reminders.isEmpty()) {
            PayrollSchedulerInfo latest = reminders.get(0);
            view.put("lastAttendanceReminderAt", latest.getInvocationTime() == null ? null : latest.getInvocationTime().toString());
            view.put("lastAttendanceReminderProcessed", latest.getProcessed());
            view.put("lastAttendanceReminderEvent", latest.getEvent());
        }
        int holidays = stores.stream()
                .mapToInt((store) -> holidayRepository.findByTenantIdAndStoreId(tenant.getTenantId(), store.getStoreId()).size())
                .sum();
        view.put("holidayCount", holidays);
        return view;
    }

    private Map<String, Object> toSupportSessionView(SupportSession session, String username) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", session.getId());
        view.put("tenantId", session.getTenantId());
        view.put("storeId", session.getStoreId());
        view.put("staffId", session.getStaffId());
        view.put("username", username);
        view.put("actorUsername", session.getActorUsername());
        view.put("expiresAt", session.getExpiresAt() == null ? null : session.getExpiresAt().toString());
        view.put("revoked", session.getRevokedAt() != null);
        view.put("active", session.isActive(Instant.now()));
        view.put("handoffConsumed", session.getHandoffConsumedAt() != null);
        return view;
    }

    private List<Map.Entry<LocalDate, String>> nationalHolidays(int year) {
        List<Map.Entry<LocalDate, String>> holidays = new ArrayList<>();
        holidays.add(Map.entry(LocalDate.of(year, 1, 1), "New Year's Day"));
        holidays.add(Map.entry(LocalDate.of(year, 1, 26), "Republic Day"));
        holidays.add(Map.entry(LocalDate.of(year, 5, 1), "Labour Day"));
        holidays.add(Map.entry(LocalDate.of(year, 8, 15), "Independence Day"));
        holidays.add(Map.entry(LocalDate.of(year, 10, 2), "Gandhi Jayanti"));
        holidays.add(Map.entry(LocalDate.of(year, 12, 25), "Christmas"));
        return holidays;
    }

    private void cloneHolidays(Long sourceTenantId, Long sourceStoreId, Long targetTenantId, Long targetStoreId) {
        if (sourceStoreId == null || targetStoreId == null) {
            return;
        }
        for (StoreHoliday holiday : holidayRepository.findByTenantIdAndStoreId(sourceTenantId, sourceStoreId)) {
            if (holidayRepository.existsByTenantIdAndStoreIdAndHolidayDate(targetTenantId, targetStoreId, holiday.getHolidayDate())) {
                continue;
            }
            StoreHoliday copy = new StoreHoliday();
            copy.setTenantId(targetTenantId);
            copy.setStoreId(targetStoreId);
            copy.setHolidayDate(holiday.getHolidayDate());
            copy.setHolidayName(holiday.getHolidayName());
            copy.setIsOptional(holiday.getIsOptional());
            holidayRepository.save(copy);
        }
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
