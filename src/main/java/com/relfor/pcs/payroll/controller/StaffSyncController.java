package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.service.ExternalStaffSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sync")
public class StaffSyncController {

    @Autowired
    private ExternalStaffSyncService staffSyncService;

    @PostMapping("/staff")
    public ResponseEntity<String> syncStaff(@RequestParam(required = false) Long tenantId) {
        staffSyncService.syncStaff(tenantId);
        return ResponseEntity.ok("Staff sync completed successfully");
    }
}
