package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.ActivityLogDTO;
import com.relfor.pcs.payroll.service.ActivityLogService;
import com.relfor.pcs.payroll.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/payroll-management/v1/activity")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_ACTIVITY_LOG)")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<ActivityLogDTO.Page> search(
            @RequestHeader(value = "X-TenantID", required = false) Long tenantId,
            @RequestHeader(value = "X-StoreID", required = false) Long storeId,
            @RequestParam(required = false) Long tenantIdParam,
            @RequestParam(required = false) Long storeIdParam,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long actorStaffId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(activityLogService.search(
                SecurityUtils.getTenantId(tenantId != null ? tenantId : tenantIdParam),
                SecurityUtils.getStoreId(storeId != null ? storeId : storeIdParam),
                module,
                action,
                actorStaffId,
                resourceType,
                resourceId,
                q,
                from,
                to,
                page,
                size
        ));
    }

    @GetMapping("/catalog")
    public ResponseEntity<ActivityLogDTO.Catalog> catalog() {
        return ResponseEntity.ok(activityLogService.catalog());
    }
}
