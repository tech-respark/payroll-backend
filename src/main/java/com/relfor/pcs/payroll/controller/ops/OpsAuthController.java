package com.relfor.pcs.payroll.controller.ops;

import com.relfor.pcs.payroll.dto.LoginRequest;
import com.relfor.pcs.payroll.dto.LoginResponse;
import com.relfor.pcs.payroll.service.ops.OpsAuthService;
import com.relfor.pcs.payroll.service.ops.OpsOnboardingService;
import com.relfor.pcs.payroll.service.ops.OpsOperatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payroll-management/v1/ops")
public class OpsAuthController {

    private final OpsAuthService opsAuthService;
    private final OpsOperatorService operatorService;
    private final OpsOnboardingService onboardingService;

    public OpsAuthController(
            OpsAuthService opsAuthService,
            OpsOperatorService operatorService,
            OpsOnboardingService onboardingService) {
        this.opsAuthService = opsAuthService;
        this.operatorService = operatorService;
        this.onboardingService = onboardingService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return opsAuthService.login(request);
    }

    @GetMapping("/support-handoff/{code}")
    public ResponseEntity<Map<String, Object>> supportHandoff(@PathVariable String code) {
        return ResponseEntity.ok(Map.of("success", true, "data", onboardingService.consumeSupportHandoff(code)));
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN, T(com.relfor.pcs.payroll.security.Permissions).SALES)")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("success", true, "data", operatorService.changeOwnPassword(body)));
    }
}
