package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.DashboardMetricsDTO;
import com.relfor.pcs.payroll.service.HRDashboardService;
import com.relfor.pcs.payroll.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll-management/v1/admin/dashboard")
@RequiredArgsConstructor
public class HRDashboardController {

    private final HRDashboardService hrDashboardService;

    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_HR_DASHBOARD)")
    public ResponseEntity<DashboardMetricsDTO> getDashboardMetrics(
            @RequestHeader(value = "X-TenantID", required = false) Long tenantId,
            @RequestHeader(value = "X-StoreID", required = false) Long storeId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
        DashboardMetricsDTO metrics = hrDashboardService.getDashboardMetrics(tenantId, storeId);
        return ResponseEntity.ok(metrics);
    }
}
