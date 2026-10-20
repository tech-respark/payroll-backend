package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.OperationsHomeDTO;
import com.relfor.pcs.payroll.dto.StaffHomeDTO;
import com.relfor.pcs.payroll.service.HomeDashboardService;
import com.relfor.pcs.payroll.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payroll-management/v1/home")
@RequiredArgsConstructor
public class HomeDashboardController {

    private final HomeDashboardService homeDashboardService;

    @GetMapping("/staff")
    public ResponseEntity<StaffHomeDTO> staffHome(
            @RequestHeader(value = "X-TenantID", required = false) Long tenantId,
            @RequestHeader(value = "X-StoreID", required = false) Long storeId) {
        return ResponseEntity.ok(homeDashboardService.staffHome(
                SecurityUtils.getTenantId(tenantId),
                SecurityUtils.getStoreId(storeId)));
    }

    @GetMapping("/operations")
    @PreAuthorize("hasAnyAuthority("
            + "T(com.relfor.pcs.payroll.security.Permissions).VIEW_OTHER_STAFF, "
            + "T(com.relfor.pcs.payroll.security.Permissions).MANAGE_STAFF, "
            + "T(com.relfor.pcs.payroll.security.Permissions).MANAGE_ROLES, "
            + "T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SHIFTS, "
            + "T(com.relfor.pcs.payroll.security.Permissions).MANAGE_ATTENDANCE, "
            + "T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, "
            + "T(com.relfor.pcs.payroll.security.Permissions).MANAGE_LEAVES, "
            + "T(com.relfor.pcs.payroll.security.Permissions).VIEW_REPORTS, "
            + "T(com.relfor.pcs.payroll.security.Permissions).VIEW_HR_DASHBOARD, "
            + "T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, "
            + "T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<OperationsHomeDTO> operationsHome(
            @RequestHeader(value = "X-TenantID", required = false) Long tenantId,
            @RequestHeader(value = "X-StoreID", required = false) Long storeId,
            @RequestParam(defaultValue = "true") boolean includeSnapshot) {
        return ResponseEntity.ok(homeDashboardService.operationsHome(
                SecurityUtils.getTenantId(tenantId),
                SecurityUtils.getStoreId(storeId),
                includeSnapshot));
    }
}
