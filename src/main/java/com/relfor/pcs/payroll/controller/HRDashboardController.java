package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.DashboardMetricsDTO;
import com.relfor.pcs.payroll.service.HRDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll-management/v1/admin/dashboard")
@RequiredArgsConstructor
public class HRDashboardController {

    private final HRDashboardService hrDashboardService;

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDTO> getDashboardMetrics(
            @RequestHeader(value = "X-TenantID", defaultValue = "1") Long tenantId,
            @RequestHeader(value = "X-StoreID", defaultValue = "1") Long storeId) {
        
        DashboardMetricsDTO metrics = hrDashboardService.getDashboardMetrics(tenantId, storeId);
        return ResponseEntity.ok(metrics);
    }
}
