package com.relfor.pcs.payroll.config;

import com.relfor.pcs.payroll.entity.AccessModule;
import com.relfor.pcs.payroll.entity.AccessPermission;
import com.relfor.pcs.payroll.entity.PlatformProduct;
import com.relfor.pcs.payroll.entity.PlatformUser;
import com.relfor.pcs.payroll.entity.Role;
import com.relfor.pcs.payroll.entity.TenantCompanyMapping;
import com.relfor.pcs.payroll.entity.TenantSubscription;
import com.relfor.pcs.payroll.repository.AccessModuleRepository;
import com.relfor.pcs.payroll.repository.PlatformProductRepository;
import com.relfor.pcs.payroll.repository.PlatformUserRepository;
import com.relfor.pcs.payroll.repository.RoleRepository;
import com.relfor.pcs.payroll.repository.TenantCompanyMappingRepository;
import com.relfor.pcs.payroll.repository.TenantSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AccessModuleRepository accessModuleRepository;

    @Autowired
    private com.relfor.pcs.payroll.repository.StoreDetailsRepository storeDetailsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PlatformUserRepository platformUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantCompanyMappingRepository tenantCompanyMappingRepository;

    @Autowired
    private TenantSubscriptionRepository tenantSubscriptionRepository;

    @Autowired
    private PlatformProductRepository platformProductRepository;

    @Override
    public void run(String... args) throws Exception {
        List<AccessModule> existing = accessModuleRepository.findByTenantIdAndStoreId(0L, 0L);
        if (existing.isEmpty()) {
            // Staff Management
            AccessModule m1 = new AccessModule();
            m1.setTenantId(0L); m1.setStoreId(0L);
            m1.setCategory("Staff Management");
            m1.setIcon("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" strokeWidth=\"2\"><path d=\"M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2\"></path><circle cx=\"9\" cy=\"7\" r=\"4\"></circle><path d=\"M23 21v-2a4 4 0 0 0-3-3.87\"></path><path d=\"M16 3.13a4 4 0 0 1 0 7.75\"></path></svg>");
            
            List<AccessPermission> p1 = new ArrayList<>();
            p1.add(createPerm("VIEW_STAFF", "View Staff Directory", m1));
            p1.add(createPerm("MANAGE_STAFF", "Create & Edit Staff", m1));
            p1.add(createPerm("VIEW_OTHER_STAFF", "View Other Staff Data", m1));
            m1.setPermissions(p1);
            accessModuleRepository.save(m1);

            // Shift & Roster
            AccessModule m2 = new AccessModule();
            m2.setTenantId(0L); m2.setStoreId(0L);
            m2.setCategory("Shift & Roster");
            m2.setIcon("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" strokeWidth=\"2\"><rect x=\"3\" y=\"4\" width=\"18\" height=\"18\" rx=\"2\" ry=\"2\"></rect><line x1=\"16\" y1=\"2\" x2=\"16\" y2=\"6\"></line><line x1=\"8\" y1=\"2\" x2=\"8\" y2=\"6\"></line><line x1=\"3\" y1=\"10\" x2=\"21\" y2=\"10\"></line></svg>");
            
            List<AccessPermission> p2 = new ArrayList<>();
            p2.add(createPerm("VIEW_SHIFTS", "View Shift Schedules", m2));
            p2.add(createPerm("MANAGE_SHIFTS", "Manage & Assign Shifts", m2));
            m2.setPermissions(p2);
            accessModuleRepository.save(m2);

            // Attendance
            AccessModule m3 = new AccessModule();
            m3.setTenantId(0L); m3.setStoreId(0L);
            m3.setCategory("Attendance");
            m3.setIcon("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" strokeWidth=\"2\"><circle cx=\"12\" cy=\"12\" r=\"10\"></circle><polyline points=\"12 6 12 12 16 14\"></polyline></svg>");
            
            List<AccessPermission> p3 = new ArrayList<>();
            p3.add(createPerm("VIEW_ATTENDANCE", "View Attendance Records", m3));
            p3.add(createPerm("MANAGE_ATTENDANCE", "Manage & Approve Attendance", m3));
            m3.setPermissions(p3);
            accessModuleRepository.save(m3);

            // Payroll & Salary
            AccessModule m4 = new AccessModule();
            m4.setTenantId(0L); m4.setStoreId(0L);
            m4.setCategory("Payroll & Salary");
            m4.setIcon("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" strokeWidth=\"2\"><line x1=\"12\" y1=\"1\" x2=\"12\" y2=\"23\"></line><path d=\"M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6\"></path></svg>");
            
            List<AccessPermission> p4 = new ArrayList<>();
            p4.add(createPerm("VIEW_SALARY", "View Salary Data", m4));
            p4.add(createPerm("MANAGE_SALARY", "Manage Payroll & Compensation", m4));
            m4.setPermissions(p4);
            accessModuleRepository.save(m4);

            // Security & Roles
            AccessModule m5 = new AccessModule();
            m5.setTenantId(0L); m5.setStoreId(0L);
            m5.setCategory("Security & Roles");
            m5.setIcon("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" strokeWidth=\"2\"><rect x=\"3\" y=\"11\" width=\"18\" height=\"11\" rx=\"2\" ry=\"2\"></rect><path d=\"M7 11V7a5 5 0 0 1 10 0v4\"></path></svg>");
            
            List<AccessPermission> p5 = new ArrayList<>();
            p5.add(createPerm("VIEW_ROLES", "View Security Roles", m5));
            p5.add(createPerm("MANAGE_ROLES", "Create & Edit Roles", m5));
            p5.add(createPerm("VIEW_ACTIVITY_LOG", "View activity log", m5));
            m5.setPermissions(p5);
            accessModuleRepository.save(m5);
            
            System.out.println("Default Access Modules (0,0) seeded successfully.");
        }

        // Always check and seed Leave Management separately just in case
        boolean hasLeaveModule = existing.stream().anyMatch(m -> "Leave Management".equals(m.getCategory()));
        if (!hasLeaveModule) {
            AccessModule mLeave = new AccessModule();
            mLeave.setTenantId(0L); mLeave.setStoreId(0L);
            mLeave.setCategory("Leave Management");
            mLeave.setIcon("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" strokeWidth=\"2\" strokeLinecap=\"round\" strokeLinejoin=\"round\"><rect x=\"3\" y=\"4\" width=\"18\" height=\"18\" rx=\"2\" ry=\"2\"></rect><line x1=\"16\" y1=\"2\" x2=\"16\" y2=\"6\"></line><line x1=\"8\" y1=\"2\" x2=\"8\" y2=\"6\"></line><line x1=\"3\" y1=\"10\" x2=\"21\" y2=\"10\"></line><path d=\"M8 14h.01\"></path><path d=\"M12 14h.01\"></path><path d=\"M16 14h.01\"></path><path d=\"M8 18h.01\"></path><path d=\"M12 18h.01\"></path><path d=\"M16 18h.01\"></path></svg>");
            
            List<AccessPermission> pLeave = new ArrayList<>();
            pLeave.add(createPerm("VIEW_LEAVES", "View Leave Records & Balances", mLeave));
            pLeave.add(createPerm("MANAGE_LEAVES", "Manage & Approve Leaves", mLeave));
            mLeave.setPermissions(pLeave);
            accessModuleRepository.save(mLeave);
            System.out.println("Leave Management module seeded successfully.");
        }

        boolean hasHospitality = existing.stream().anyMatch(m -> "Hospitality".equals(m.getCategory()));
        if (!hasHospitality) {
            AccessModule hospitality = new AccessModule();
            hospitality.setTenantId(0L);
            hospitality.setStoreId(0L);
            hospitality.setCategory("Hospitality");
            hospitality.setIcon("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" strokeWidth=\"2\"><path d=\"M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z\"></path></svg>");
            List<AccessPermission> hospitalityPerms = new ArrayList<>();
            hospitalityPerms.add(createPerm("VIEW_HOSPITALITY", "View hospitality payroll tools", hospitality));
            hospitalityPerms.add(createPerm("MANAGE_HOSPITALITY", "Manage hospitality payroll tools", hospitality));
            hospitality.setPermissions(hospitalityPerms);
            accessModuleRepository.save(hospitality);
        }

        // Seed Store 1 config
        java.util.Optional<com.relfor.pcs.payroll.entity.StoreDetails> sdOpt = 
            storeDetailsRepository.fetchStoreAndTenantDetails( 1L, 1L);
        if (sdOpt.isPresent()) {
            com.relfor.pcs.payroll.entity.StoreDetails sd = sdOpt.get();
            boolean updated = false;
            if (sd.getFinancialYearStartMonth() == null) {
                sd.setFinancialYearStartMonth(4); // April
                updated = true;
            }
            if (sd.getPayrollLockedUpToDate() == null) {
                sd.setPayrollLockedUpToDate(java.time.LocalDate.of(2026, 3, 31));
                updated = true;
            }
            if (sd.getStoreOpenTime() == null) {
                sd.setStoreOpenTime("09:00");
                updated = true;
            }
            if (sd.getStoreCloseTime() == null) {
                sd.setStoreCloseTime("18:00");
                updated = true;
            }
            if (updated) {
                storeDetailsRepository.save(sd);
                System.out.println("Store 1 configurations seeded.");
            }
        }

        ensureActivityLogPermission();
        ensureMissingCatalogPermissions();
        ensureMasterPlatformUser();
        ensureSubscriptionsForExistingTenants();
        ensureSubscriptionSalesOwners();
        ensurePlatformProducts();
    }

    private AccessPermission createPerm(String id, String label, AccessModule module) {
        AccessPermission ap = new AccessPermission();
        ap.setPermissionId(id);
        ap.setLabel(label);
        ap.setAccessModule(module);
        return ap;
    }

    private void ensureActivityLogPermission() {
        List<AccessModule> modules = accessModuleRepository.findByTenantIdAndStoreId(0L, 0L);
        AccessModule security = modules.stream()
                .filter(m -> "Security & Roles".equals(m.getCategory()))
                .findFirst()
                .orElse(null);
        if (security != null) {
            boolean exists = security.getPermissions() != null && security.getPermissions().stream()
                    .anyMatch(p -> "VIEW_ACTIVITY_LOG".equals(p.getPermissionId()));
            if (!exists) {
                if (security.getPermissions() == null) {
                    security.setPermissions(new ArrayList<>());
                }
                security.getPermissions().add(createPerm("VIEW_ACTIVITY_LOG", "View activity log", security));
                accessModuleRepository.save(security);
                System.out.println("VIEW_ACTIVITY_LOG permission seeded.");
            }
        }

        for (Role role : roleRepository.findAll()) {
            String perms = role.getPermissions();
            if (perms == null || perms.isBlank() || perms.contains("VIEW_ACTIVITY_LOG")) {
                continue;
            }
            if (perms.contains("MANAGE_ROLES") || perms.contains("VIEW_HR_DASHBOARD")) {
                role.setPermissions(perms + ",VIEW_ACTIVITY_LOG");
                roleRepository.save(role);
            }
        }
    }

    private void ensureMissingCatalogPermissions() {
        List<AccessModule> modules = accessModuleRepository.findByTenantIdAndStoreId(0L, 0L);
        AccessModule payroll = findModule(modules, "Payroll & Salary");
        if (payroll != null) {
            addPermissionIfMissing(payroll, "VIEW_REPORTS", "View payroll and attendance reports");
        }
        AccessModule staff = findModule(modules, "Staff Management");
        if (staff != null) {
            addPermissionIfMissing(staff, "VIEW_HR_DASHBOARD", "View HR dashboard metrics");
        }
        AccessModule shifts = findModule(modules, "Shift & Roster");
        if (shifts != null) {
            addPermissionIfMissing(shifts, "MANAGE_STORE_SETTINGS", "Manage store hours and settings");
        }
    }

    private AccessModule findModule(List<AccessModule> modules, String category) {
        return modules.stream().filter(m -> category.equals(m.getCategory())).findFirst().orElse(null);
    }

    private void addPermissionIfMissing(AccessModule module, String permissionId, String label) {
        boolean exists = module.getPermissions() != null && module.getPermissions().stream()
                .anyMatch(p -> permissionId.equals(p.getPermissionId()));
        if (exists) {
            return;
        }
        if (module.getPermissions() == null) {
            module.setPermissions(new ArrayList<>());
        }
        module.getPermissions().add(createPerm(permissionId, label, module));
        accessModuleRepository.save(module);
    }

    private void ensureMasterPlatformUser() {
        if (platformUserRepository.count() > 0) {
            return;
        }
        PlatformUser master = new PlatformUser();
        master.setUsername("superadmin");
        master.setDisplayName("Super Admin");
        master.setEmail("superadmin@local");
        master.setRole(PlatformUser.Role.SUPERADMIN);
        master.setActive(true);
        master.setPasswordHash(passwordEncoder.encode("sadmin1234"));
        platformUserRepository.save(master);
    }

    private void ensureSubscriptionsForExistingTenants() {
        for (TenantCompanyMapping tenant : tenantCompanyMappingRepository.findAll()) {
            if (tenantSubscriptionRepository.findByTenantId(tenant.getTenantId()).isPresent()) {
                continue;
            }
            TenantSubscription subscription = new TenantSubscription();
            subscription.setTenantId(tenant.getTenantId());
            subscription.setStatus(TenantSubscription.Status.ACTIVE);
            subscription.setPlanCode(TenantSubscription.PlanCode.STANDARD);
            platformUserRepository.findAllByOrderByIdAsc().stream().findFirst()
                    .ifPresent((owner) -> subscription.setSalesOwnerId(owner.getId()));
            tenantSubscriptionRepository.save(subscription);
        }
    }

    private void ensureSubscriptionSalesOwners() {
        Long ownerId = platformUserRepository.findAllByOrderByIdAsc().stream()
                .findFirst()
                .map(PlatformUser::getId)
                .orElse(null);
        if (ownerId == null) {
            return;
        }
        for (TenantSubscription subscription : tenantSubscriptionRepository.findAll()) {
            if (subscription.getSalesOwnerId() == null) {
                subscription.setSalesOwnerId(ownerId);
                tenantSubscriptionRepository.save(subscription);
            }
        }
    }

    private void ensurePlatformProducts() {
        ensureProduct(
                "devourin",
                "Devourin",
                "First-party billing suite. Default source product for existing and new tenants until a connector exists.",
                PlatformProduct.Category.BILLING,
                PlatformProduct.Origin.FIRST_PARTY,
                10);
        ensureProduct(
                "respark",
                "Respark",
                "First-party billing suite. Cataloged alongside Devourin. Add more products from ops when a public integration is needed.",
                PlatformProduct.Category.BILLING,
                PlatformProduct.Origin.FIRST_PARTY,
                20);
        Long defaultId = platformProductRepository.findFirstByDefaultProductTrue()
                .or(() -> platformProductRepository.findByCodeIgnoreCase("devourin"))
                .map(PlatformProduct::getId)
                .orElse(null);
        if (defaultId == null) {
            return;
        }
        for (TenantCompanyMapping tenant : tenantCompanyMappingRepository.findAll()) {
            if (tenant.getProductId() == null) {
                tenant.setProductId(defaultId);
                tenantCompanyMappingRepository.save(tenant);
            }
        }
    }

    private void ensureProduct(
            String code,
            String displayName,
            String description,
            PlatformProduct.Category category,
            PlatformProduct.Origin origin,
            int sortOrder) {
        PlatformProduct product = platformProductRepository.findByCodeIgnoreCase(code).orElseGet(PlatformProduct::new);
        boolean creating = product.getId() == null;
        if (creating) {
            product.setCode(code);
            product.setDisplayName(displayName);
            product.setDescription(description);
            product.setCategory(category);
            product.setOrigin(origin);
            product.setIntegrationMode(PlatformProduct.IntegrationMode.STANDALONE);
            product.setActive(true);
            product.setSortOrder(sortOrder);
            product.setDefaultProduct(false);
        } else {
            if (product.getDisplayName() == null || product.getDisplayName().isBlank()) {
                product.setDisplayName(displayName);
            }
            if (product.getDescription() == null || product.getDescription().isBlank()) {
                product.setDescription(description);
            }
            if (product.getCategory() == null) {
                product.setCategory(category);
            }
            if (product.getOrigin() == null) {
                product.setOrigin(origin);
            }
            if (product.getIntegrationMode() == null) {
                product.setIntegrationMode(PlatformProduct.IntegrationMode.STANDALONE);
            }
            if (product.getActive() == null) {
                product.setActive(true);
            }
            if (product.getDefaultProduct() == null) {
                product.setDefaultProduct(false);
            }
        }
        platformProductRepository.save(product);
        if (platformProductRepository.findFirstByDefaultProductTrue().isEmpty()) {
            platformProductRepository.findByCodeIgnoreCase("devourin").ifPresent((row) -> {
                row.setDefaultProduct(true);
                row.setActive(true);
                platformProductRepository.save(row);
            });
        }
    }
}
