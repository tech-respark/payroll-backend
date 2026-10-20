package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.service.ExternalStaffSyncService;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sync")
public class StaffSyncController {

    @Autowired
    private ExternalStaffSyncService staffSyncService;

    @PostMapping("/staff")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_STAFF)")
    public ResponseEntity<String> syncStaff(@RequestParam(required = false) Long tenantId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        staffSyncService.syncStaff(tenantId);
        return ResponseEntity.ok("Staff sync completed successfully");
    }
}
