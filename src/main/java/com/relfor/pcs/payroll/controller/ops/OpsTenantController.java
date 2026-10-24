package com.relfor.pcs.payroll.controller.ops;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityResourceTypes;
import com.relfor.pcs.payroll.service.ops.OpsOnboardingService;
import com.relfor.pcs.payroll.service.ops.OpsOperatorService;
import com.relfor.pcs.payroll.service.ops.OpsSalaryPackService;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payroll-management/v1/ops")
@PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN, T(com.relfor.pcs.payroll.security.Permissions).SALES)")
public class OpsTenantController {

    private final OpsOnboardingService onboardingService;
    private final OpsSalaryPackService salaryPackService;
    private final OpsOperatorService operatorService;
    private final ActivityRecorder activityRecorder;

    public OpsTenantController(
            OpsOnboardingService onboardingService,
            OpsSalaryPackService salaryPackService,
            OpsOperatorService operatorService,
            ActivityRecorder activityRecorder) {
        this.onboardingService = onboardingService;
        this.salaryPackService = salaryPackService;
        this.operatorService = operatorService;
        this.activityRecorder = activityRecorder;
    }

    @GetMapping("/tenants")
    public ResponseEntity<Map<String, Object>> list() {
        return ok(onboardingService.listTenants());
    }

    @GetMapping("/sales-owners")
    public ResponseEntity<Map<String, Object>> salesOwners() {
        return ok(operatorService.listSalesOwners());
    }

    @GetMapping("/tenants/{tenantId}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long tenantId) {
        return ok(onboardingService.getTenant(tenantId));
    }

    @PostMapping("/tenants/{tenantId}/holiday-pack")
    public ResponseEntity<Map<String, Object>> holidayPack(
            @PathVariable Long tenantId,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Integer year) {
        return ok(onboardingService.applyHolidayPack(tenantId, storeId, year));
    }

    @PostMapping("/tenants/{tenantId}/admins/{staffId}/reset-password")
    public ResponseEntity<Map<String, Object>> resetAdminPassword(
            @PathVariable Long tenantId,
            @PathVariable Long staffId,
            @RequestBody Map<String, Object> body) {
        return ok(onboardingService.resetAdminPassword(tenantId, staffId, body));
    }

    @PutMapping("/tenants/{tenantId}/admins/{staffId}")
    public ResponseEntity<Map<String, Object>> setAdminActive(
            @PathVariable Long tenantId,
            @PathVariable Long staffId,
            @RequestBody Map<String, Object> body) {
        boolean active = body.get("active") == null || Boolean.TRUE.equals(body.get("active"))
                || "true".equalsIgnoreCase(String.valueOf(body.get("active")));
        return ok(onboardingService.setAdminActive(tenantId, staffId, active));
    }

    @PostMapping("/tenants")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ok(onboardingService.createTenant(body));
    }

    @PutMapping("/tenants/{tenantId}")
    public ResponseEntity<Map<String, Object>> updateTenant(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> body) {
        return ok(onboardingService.updateTenant(tenantId, body));
    }

    @PostMapping("/tenants/{tenantId}/stores")
    public ResponseEntity<Map<String, Object>> addStore(@PathVariable Long tenantId, @RequestBody Map<String, Object> body) {
        return ok(onboardingService.addStore(tenantId, body));
    }

    @PutMapping("/tenants/{tenantId}/stores/{storeId}")
    public ResponseEntity<Map<String, Object>> updateStore(
            @PathVariable Long tenantId,
            @PathVariable Long storeId,
            @RequestBody Map<String, Object> body) {
        return ok(onboardingService.updateStore(tenantId, storeId, body));
    }

    @PostMapping("/tenants/{tenantId}/admin")
    public ResponseEntity<Map<String, Object>> createAdmin(@PathVariable Long tenantId, @RequestBody Map<String, Object> body) {
        return ok(onboardingService.createAdmin(tenantId, body));
    }

    @GetMapping("/tenants/{tenantId}/salary-definitions")
    public ResponseEntity<Map<String, Object>> salaryList(
            @PathVariable Long tenantId,
            @RequestParam(required = false, defaultValue = "0") Long storeId) {
        onboardingService.assertCanAccess(tenantId);
        return ok(salaryPackService.list(tenantId, storeId));
    }

    @PostMapping("/tenants/{tenantId}/salary-definitions")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN)")
    public ResponseEntity<Map<String, Object>> upsertSalary(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> body) {
        onboardingService.assertCanAccess(tenantId);
        return ok(salaryPackService.upsertDefinition(tenantId, body));
    }

    @DeleteMapping("/tenants/{tenantId}/salary-definitions/{definitionId}")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN)")
    public ResponseEntity<Map<String, Object>> deleteSalary(
            @PathVariable Long tenantId,
            @PathVariable Long definitionId) {
        onboardingService.assertCanAccess(tenantId);
        salaryPackService.deleteDefinition(tenantId, definitionId);
        return ok(Map.of("deleted", true));
    }

    @PostMapping("/tenants/{tenantId}/salary-pack")
    public ResponseEntity<Map<String, Object>> applySalaryPack(
            @PathVariable Long tenantId,
            @RequestBody(required = false) Map<String, Object> body) {
        onboardingService.assertCanAccess(tenantId);
        if (body != null && body.get("sourceTenantId") != null) {
            Long source = Long.parseLong(String.valueOf(body.get("sourceTenantId")));
            onboardingService.assertCanAccess(source);
            return ok(salaryPackService.cloneFrom(source, tenantId));
        }
        return ok(salaryPackService.applySystemPack(tenantId));
    }

    @PostMapping("/tenants/{tenantId}/salary-preview")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN)")
    public ResponseEntity<Map<String, Object>> previewSalary(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> body) {
        onboardingService.assertCanAccess(tenantId);
        @SuppressWarnings("unchecked")
        Map<String, Object> inputs = body.get("inputs") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
        return ok(salaryPackService.preview(String.valueOf(body.get("formula")), inputs));
    }

    @PostMapping("/tenants/{tenantId}/leave-pack")
    public ResponseEntity<Map<String, Object>> leavePack(
            @PathVariable Long tenantId,
            @RequestParam(required = false) Long storeId) {
        return ok(onboardingService.applyLeavePack(tenantId, storeId));
    }

    @PostMapping("/tenants/{tenantId}/default-shift")
    public ResponseEntity<Map<String, Object>> defaultShift(
            @PathVariable Long tenantId,
            @RequestParam(required = false) Long storeId) {
        return ok(onboardingService.applyDefaultShift(tenantId, storeId));
    }

    @PutMapping("/tenants/{tenantId}/integrations")
    public ResponseEntity<Map<String, Object>> integrations(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> body) {
        return ok(onboardingService.updateIntegrations(tenantId, body));
    }

    @PostMapping("/tenants/{tenantId}/statutory")
    public ResponseEntity<Map<String, Object>> statutory(
            @PathVariable Long tenantId,
            @RequestParam(required = false) Long storeId) {
        return ok(onboardingService.persistStatutory(tenantId, storeId));
    }

    @PutMapping("/tenants/{tenantId}/subscription")
    public ResponseEntity<Map<String, Object>> subscription(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> body) {
        return ok(onboardingService.updateSubscription(tenantId, body));
    }

    @PostMapping("/tenants/{tenantId}/clone-from/{sourceTenantId}")
    public ResponseEntity<Map<String, Object>> cloneFrom(
            @PathVariable Long tenantId,
            @PathVariable Long sourceTenantId) {
        return ok(onboardingService.cloneFrom(tenantId, sourceTenantId));
    }

    @PostMapping("/tenants/{tenantId}/stores/{storeId}/support-session")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN)")
    public ResponseEntity<Map<String, Object>> supportSession(
            @PathVariable Long tenantId,
            @PathVariable Long storeId,
            @RequestParam Long staffId) {
        Map<String, Object> session = onboardingService.createSupportSession(tenantId, storeId, staffId);
        activityRecorder.recordSafely(ActivityEvent.builder()
                .action(ActivityActions.AUTH_SUPPORT_SESSION)
                .tenantId(tenantId)
                .storeId(storeId)
                .resourceType(ActivityResourceTypes.SESSION)
                .resourceId(staffId)
                .resourceLabel(String.valueOf(session.get("username")))
                .summary("Minted a 30-minute support session for " + session.get("username"))
                .build());
        return ok(session);
    }

    @PostMapping("/tenants/{tenantId}/support-sessions/{sessionId}/revoke")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN)")
    public ResponseEntity<Map<String, Object>> revokeSupportSession(
            @PathVariable Long tenantId,
            @PathVariable Long sessionId) {
        return ok(onboardingService.revokeSupportSession(tenantId, sessionId));
    }

    @GetMapping("/environment")
    public ResponseEntity<Map<String, Object>> environment() {
        return ok(onboardingService.environment());
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data, "actor", String.valueOf(SecurityUtils.currentUsername())));
    }
}
