package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.*;
import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Collections;

@Service
public class ExternalStaffSyncService {

    @Autowired
    private TenantCompanyMappingRepository tenantMappingRepo;
    
    @Autowired
    private StoreDetailsRepository storeDetailsRepo;
    
    @Autowired
    private RoleRepository roleRepo;
    
    @Autowired
    private PersonnelDetailsRepository personnelRepo;
    
    @Autowired
    private StoreStaffRoleRepository storeStaffRoleRepo;

    public void syncStaff(Long tenantId) {
        RestTemplate restTemplate = new RestTemplate();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        List<TenantCompanyMapping> mappings;
        if (tenantId != null) {
            mappings = tenantMappingRepo.findById(tenantId).map(Collections::singletonList).orElse(Collections.emptyList());
        } else {
            mappings = tenantMappingRepo.findAll();
        }

        for (TenantCompanyMapping tenant : mappings) {
            String baseUrl = tenant.getExternalSoftwareUrl();
            String appName = tenant.getExternalAppName();
            
            if (baseUrl == null || baseUrl.isEmpty() || appName == null || appName.isEmpty()) {
                continue; // Skip if this tenant isn't configured for external sync
            }

            List<StoreDetails> stores = storeDetailsRepo.findByTenantCompanyMappingId(tenant.getId());
            for (StoreDetails store : stores) {
                String branchId = store.getExternalBranchId();
                if (branchId == null || branchId.isEmpty()) continue;

                try {
                    // Sync Roles
                    String rolesUrl = baseUrl + "/" + appName + "/getRoles?branchId=" + branchId;
                    ExternalRolesResponse rolesResponse = restTemplate.getForObject(rolesUrl, ExternalRolesResponse.class);
                    
                    if (rolesResponse != null && rolesResponse.getRoles() != null) {
                        for (ExternalRoleDTO roleDto : rolesResponse.getRoles()) {
                            Role existingRole = roleRepo.findByNameAndTenantId(roleDto.getRoleName(), tenant.getTenantId());
                            if (existingRole == null) {
                                Role newRole = new Role();
                                newRole.setName(roleDto.getRoleName());
                                newRole.setDescription(roleDto.getDescription());
                                newRole.setTenantId(tenant.getTenantId());
                                newRole.setPermissions(""); // Zero permissions by default
                                newRole.setActive(1);
                                roleRepo.save(newRole);
                            }
                        }
                    }

                    // Sync Staff
                    String staffUrl = baseUrl + "/" + appName + "/getStaffs?branchId=" + branchId;
                    ExternalStaffsResponse staffResponse = restTemplate.getForObject(staffUrl, ExternalStaffsResponse.class);
                    
                    if (staffResponse != null && staffResponse.getStaff() != null) {
                        for (ExternalStaffDTO staffDto : staffResponse.getStaff()) {
                            // Find or create personnel
                            PersonnelDetails pd = null;
                            List<PersonnelDetails> matches = personnelRepo.getPersonnelByEmployeeCode(tenant.getTenantId(), Collections.singletonList(staffDto.getExternalStaffId()));
                            if (!matches.isEmpty()) {
                                pd = matches.get(0);
                            } else {
                                pd = new PersonnelDetails();
                                pd.setEmployeeCode(staffDto.getExternalStaffId());
                                pd.setApplicationTenantId(tenant.getTenantId());
                                pd.setApplicationName(tenant.getApplicationName());
                                pd.setPassword(passwordEncoder.encode("Welcome@123")); // default password
                                pd.setExperience(0.0f); // Default mandatory field
                            }
                            
                            pd.setFirstName(staffDto.getFirstName());
                            pd.setLastName(staffDto.getLastName());
                            pd.setEmail(staffDto.getEmail());
                            pd.setPhone(staffDto.getPhone());
                            pd.setGender(staffDto.getGender());
                            pd.setUsername(staffDto.getEmail()); // Using email as username
                            pd.setActive(Boolean.TRUE.equals(staffDto.getIsActive()));
                            
                            pd = personnelRepo.save(pd);

                            // Link to Role and Store
                            if (staffDto.getRoleName() != null && !staffDto.getRoleName().isEmpty()) {
                                Role assignedRole = roleRepo.findByNameAndTenantId(staffDto.getRoleName(), tenant.getTenantId());
                                if (assignedRole != null) {
                                    StoreStaffRole mapping = storeStaffRoleRepo.findByStaffIdAndStoreIdAndActive(pd.getId(), store.getStoreId(), 1);
                                    if (mapping == null) {
                                        mapping = new StoreStaffRole();
                                        mapping.setStaffId(pd.getId());
                                        mapping.setStoreId(store.getStoreId());
                                        mapping.setTenantId(tenant.getTenantId());
                                        mapping.setActive(1);
                                    }
                                    mapping.setRoleId(assignedRole.getId());
                                    storeStaffRoleRepo.save(mapping);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error syncing for tenant " + tenant.getTenantId() + " store " + store.getStoreId() + ": " + e.getMessage());
                }
            }
        }
    }
}
