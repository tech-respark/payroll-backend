package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.RegularizationApplyRequest;
import com.relfor.pcs.payroll.dto.RegularizationApprovalRequest;
import com.relfor.pcs.payroll.entity.AttendanceRegularizationRequest;
import com.relfor.pcs.payroll.service.AttendanceRegularizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/payroll-management/v1/regularizations")
@RequiredArgsConstructor
public class AttendanceRegularizationController {

    private final AttendanceRegularizationService regularizationService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRegularizations(@RequestParam Long tenantId, @RequestParam Long storeId, org.springframework.security.core.Authentication authentication) {
        try {
            Long managerId = null;
            boolean isHrAdmin = false;
            if (authentication != null && authentication.getPrincipal() instanceof com.relfor.pcs.payroll.security.CustomUserDetails) {
                com.relfor.pcs.payroll.security.CustomUserDetails userDetails = (com.relfor.pcs.payroll.security.CustomUserDetails) authentication.getPrincipal();
                managerId = userDetails.getStaffId();
                isHrAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_HR_ADMIN") || a.getAuthority().equals("HR_ADMIN") 
                                    || a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("SUPER_ADMIN"));
            }
            return ResponseEntity.ok(regularizationService.getPendingRegularizations(tenantId, storeId, managerId, isHrAdmin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForRegularization(@Valid @RequestBody RegularizationApplyRequest payload) {
        try {
            AttendanceRegularizationRequest request = regularizationService.requestRegularization(
                payload.getStaffId(), payload.getDate(), payload.getInTime(), payload.getOutTime(), payload.getReason()
            );
            return ResponseEntity.ok(Map.of("message", "Regularization request submitted", "requestId", request.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRegularization(@PathVariable Long requestId, @Valid @RequestBody RegularizationApprovalRequest payload) {
        try {
            regularizationService.approveRegularization(requestId, payload.getApproverId());
            return ResponseEntity.ok(Map.of("message", "Regularization request approved and attendance synced."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRegularization(@PathVariable Long requestId, @Valid @RequestBody RegularizationApprovalRequest payload) {
        try {
            regularizationService.rejectRegularization(requestId, payload.getApproverId());
            return ResponseEntity.ok(Map.of("message", "Regularization request rejected."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
