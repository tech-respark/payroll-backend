package com.relfor.pcs.payroll.config;

import com.relfor.pcs.payroll.entity.AccessModule;
import com.relfor.pcs.payroll.entity.AccessPermission;
import com.relfor.pcs.payroll.repository.AccessModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private AccessModuleRepository accessModuleRepository;

    @Autowired
    private com.relfor.pcs.payroll.repository.StoreDetailsRepository storeDetailsRepository;

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
            m5.setPermissions(p5);
            accessModuleRepository.save(m5);
            
            System.out.println("Default Access Modules (0,0) seeded successfully.");
        }

        // Seed Store 1 config
        java.util.Optional<com.relfor.pcs.payroll.entity.StoreDetails> sdOpt = 
            storeDetailsRepository.fetchStoreAndTenantDetails("RESPARK", 1L, 1L);
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
    }

    private AccessPermission createPerm(String id, String label, AccessModule module) {
        AccessPermission ap = new AccessPermission();
        ap.setPermissionId(id);
        ap.setLabel(label);
        ap.setAccessModule(module);
        return ap;
    }
}
