package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.RoleAssignRequest;
import com.relfor.pcs.payroll.dto.RoleCreateRequest;
import com.relfor.pcs.payroll.entity.Role;
import com.relfor.pcs.payroll.entity.StoreStaffRole;
import com.relfor.pcs.payroll.repository.RoleRepository;
import com.relfor.pcs.payroll.repository.StoreStaffRoleRepository;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll-management/v1/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StoreStaffRoleRepository storeStaffRoleRepository;

    @Autowired
    private com.relfor.pcs.payroll.repository.AccessModuleRepository accessModuleRepository;

    @GetMapping("/permissions/modules")
    public ResponseEntity<Map<String, Object>> getModules(@RequestParam Long tenantId, @RequestParam Long storeId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
        List<com.relfor.pcs.payroll.entity.AccessModule> modules = accessModuleRepository.findByTenantIdAndStoreId(tenantId, storeId);
        if (modules.isEmpty() && (tenantId != 0 || storeId != 0)) {
            modules = accessModuleRepository.findByTenantIdAndStoreId(0L, 0L);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", modules);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoles(@RequestParam Long tenantId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        List<Role> roles = roleRepository.findByActiveAndTenantId(1, tenantId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", roles);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody RoleCreateRequest request) {
        request.setTenantId(SecurityUtils.getTenantId(request.getTenantId()));
        request.setStoreId(SecurityUtils.getStoreId(request.getStoreId()));
        Role newRole = new Role();
        newRole.setName(request.getName());
        newRole.setDescription(request.getDescription());
        newRole.setTenantId(request.getTenantId());
        
        // Use request values or default if null
        newRole.setActive(request.getActive() != null ? request.getActive() : 1);
        newRole.setHideFromUi(request.getHideFromUi() != null ? request.getHideFromUi() : false);
//        newRole.setRIndex(request.getRIndex() != null ? request.getRIndex() : 0);
//        newRole.setRValue(request.getRValue() != null ? request.getRValue() : 0);
        newRole.setRestrictionDays(request.getRestrictionDays() != null ? request.getRestrictionDays() : 0L);

        if (request.getPermissions() != null) {
            newRole.setPermissions(String.join(",", request.getPermissions()));
        }
        if (request.getAssignedReports() != null) {
            newRole.setAssignedReports(String.join(",", request.getAssignedReports()));
        }

        Role savedRole = roleRepository.save(newRole);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Role created successfully");
        response.put("data", savedRole);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateRole(@RequestBody RoleCreateRequest request, @RequestParam Long roleId) {
        Role existingRole = roleRepository.findById(roleId).orElse(null);
        Map<String, Object> response = new HashMap<>();

        if (existingRole == null) {
            response.put("success", false);
            response.put("message", "Role not found");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.getName() != null) {
            existingRole.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingRole.setDescription(request.getDescription());
        }
        if (request.getPermissions() != null) {
            existingRole.setPermissions(String.join(",", request.getPermissions()));
        }
        if (request.getAssignedReports() != null) {
            existingRole.setAssignedReports(String.join(",", request.getAssignedReports()));
        }
        if (request.getActive() != null) {
            existingRole.setActive(request.getActive());
        }
        if (request.getHideFromUi() != null) {
            existingRole.setHideFromUi(request.getHideFromUi());
        }
//        if (request.getRIndex() != null) {
//            existingRole.setRIndex(request.getRIndex());
//        }
//        if (request.getRValue() != null) {
//            existingRole.setRValue(request.getRValue());
//        }
        if (request.getRestrictionDays() != null) {
            existingRole.setRestrictionDays(request.getRestrictionDays());
        }

        Role savedRole = roleRepository.save(existingRole);

        response.put("success", true);
        response.put("message", "Role updated successfully");
        response.put("data", savedRole);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignRole(@RequestBody RoleAssignRequest request) {
        request.setTenantId(SecurityUtils.getTenantId(request.getTenantId()));
        request.setStoreId(SecurityUtils.getStoreId(request.getStoreId()));
        
        StoreStaffRole assignment = new StoreStaffRole();

        // Deactivate existing roles for this staff
        List<StoreStaffRole> existingAssignments = storeStaffRoleRepository.findByStaffIdAndActive(request.getStaffId(), 1);
        for (StoreStaffRole existing : existingAssignments) {
            existing.setActive(0);
            existing.setSystemUpdatedOn(LocalDateTime.now());
        }
        if (!existingAssignments.isEmpty()) {
            storeStaffRoleRepository.saveAll(existingAssignments);
        }

        assignment.setStaffId(request.getStaffId());
        assignment.setRoleId(request.getRoleId());
        assignment.setStoreId(request.getStoreId());
        assignment.setTenantId(request.getTenantId());
        assignment.setActive(1);
        assignment.setEnableAppointments(0);
        assignment.setSystemCreatedOn(LocalDateTime.now());
        assignment.setSystemUpdatedOn(LocalDateTime.now());

        storeStaffRoleRepository.save(assignment);

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
        response.put("message", "Role assigned successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<Map<String, Object>> getStaffRole(@PathVariable Long staffId) {
        Map<String, Object> response = new HashMap<>();
        List<Role> activeRoles = storeStaffRoleRepository.findActiveRolesByStaffId(staffId);
        
        if (!activeRoles.isEmpty()) {
            response.put("success", true);
            response.put("data", activeRoles.get(0)); // Return the primary active role
        } else {
            response.put("success", false);
            response.put("message", "No active role found");
        }
        return ResponseEntity.ok(response);
    }
}
