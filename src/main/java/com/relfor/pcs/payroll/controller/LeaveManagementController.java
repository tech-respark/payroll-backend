package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.entity.LeaveApplication;
import com.relfor.pcs.payroll.entity.LeavePlanRule;
import com.relfor.pcs.payroll.entity.LeaveType;
import com.relfor.pcs.payroll.repository.LeaveTypeRepository;
import com.relfor.pcs.payroll.service.LeaveLedgerService;
import com.relfor.pcs.payroll.service.LeaveManagementService;
import lombok.RequiredArgsConstructor;
import com.relfor.pcs.payroll.dto.ActionApprovalRequest;
import com.relfor.pcs.payroll.dto.LeaveApplicationRequest;

import com.relfor.pcs.payroll.security.Permissions;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payroll-management/v1/leaves")
@RequiredArgsConstructor
public class LeaveManagementController {

    private final LeaveManagementService leaveManagementService;
    private final LeaveLedgerService leaveLedgerService;
    private final LeaveTypeRepository leaveTypeRepository;

    @GetMapping("/types/{staffId}")
    public ResponseEntity<?> getEligibleLeaveTypes(@PathVariable Long staffId) {
        SecurityUtils.requireSelfOrAnyAuthority(staffId, Permissions.MANAGE_LEAVES);
        try {
            return ResponseEntity.ok(leaveManagementService.getEligibleLeaveTypes(staffId));
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "Unable to load leave types";
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }
    }

    @GetMapping("/applications/staff/{staffId}")
    public ResponseEntity<?> getStaffLeaveHistory(@PathVariable Long staffId) {
        SecurityUtils.requireSelfOrAnyAuthority(staffId, Permissions.MANAGE_LEAVES);
        try {
            return ResponseEntity.ok(leaveManagementService.getStaffLeaveHistory(staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/applications/pending")
    public ResponseEntity<?> getPendingLeaves(@RequestParam Long tenantId, @RequestParam Long storeId, org.springframework.security.core.Authentication authentication) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);
        try {
            Long managerId = null;
            boolean isHrAdmin = false;
            if (authentication != null && authentication.getPrincipal() instanceof com.relfor.pcs.payroll.security.CustomUserDetails) {
                com.relfor.pcs.payroll.security.CustomUserDetails userDetails = (com.relfor.pcs.payroll.security.CustomUserDetails) authentication.getPrincipal();
                managerId = userDetails.getStaffId();
                isHrAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(Permissions.MANAGE_LEAVES)
                                    || a.getAuthority().equals("ROLE_HR_ADMIN") || a.getAuthority().equals("HR_ADMIN")
                                    || a.getAuthority().equals("ROLE_SUPER_ADMIN") || a.getAuthority().equals("SUPER_ADMIN"));
            }
            return ResponseEntity.ok(leaveManagementService.getPendingLeaves(tenantId, storeId, managerId, isHrAdmin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(@Valid @RequestBody LeaveApplicationRequest payload) {
        try {
            Long actorId = SecurityUtils.getStaffId();
            if (actorId != null && payload.getStaffId() != null
                    && !payload.getStaffId().equals(actorId)
                    && !SecurityUtils.hasAuthority(Permissions.MANAGE_LEAVES)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only apply leave for yourself."));
            }
            LeaveType leaveType = leaveTypeRepository.findById(payload.getLeaveTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Leave Type ID"));

            LeaveApplication application = leaveManagementService.applyForLeave(
                payload.getTenantId(), payload.getStoreId(), payload.getStaffId(), 
                leaveType, payload.getStartDate(), payload.getEndDate(), 
                payload.getReason(), payload.getAttachmentUrl(), payload.getLeaveSession()
            );
            return ResponseEntity.ok(Map.of("message", "Leave application submitted successfully", "applicationId", application.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/balances/{staffId}")
    public ResponseEntity<?> getAvailableBalances(@PathVariable Long staffId, @RequestParam Long leaveTypeId) {
        SecurityUtils.requireSelfOrAnyAuthority(staffId, Permissions.MANAGE_LEAVES);
        try {
            BigDecimal balance = leaveLedgerService.getAvailableBalance(staffId, leaveTypeId, LocalDate.now());
            return ResponseEntity.ok(Map.of("staffId", staffId, "leaveTypeId", leaveTypeId, "availableBalance", balance));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/balances/all/{staffId}")
    public ResponseEntity<?> getAvailableBalancesForAllTypes(@PathVariable Long staffId, 
                                                             @RequestParam(required = false) Long tenantId, 
                                                             @RequestParam(required = false) Long storeId) {
        SecurityUtils.requireSelfOrAnyAuthority(staffId, Permissions.MANAGE_LEAVES);
        try {
            List<LeavePlanRule> eligibleRules = leaveManagementService.getEligibleLeaveRules(staffId);
            java.util.List<Map<String, Object>> balances = new java.util.ArrayList<>();
            for (LeavePlanRule rule : eligibleRules) {
                LeaveType type = rule.getLeaveType();
                Long typeId = type.getId();
                
                BigDecimal balance = leaveLedgerService.getAvailableBalance(staffId, typeId, LocalDate.now());
                
                balances.add(Map.of(
                    "typeId", typeId,
                    "typeName", type.getLeaveName(),
                    "code", type.getLeaveCode(),
                    "available", balance,
                    "annualAllotment", rule.getAnnualAllotment()
                ));
            }
            return ResponseEntity.ok(balances);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{applicationId}/approve")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_LEAVES)")
    public ResponseEntity<?> approveLeave(@PathVariable Long applicationId, @Valid @RequestBody ActionApprovalRequest payload) {
        try {
            Long managerId = resolveManagerId(payload);
            leaveManagementService.approveLeave(applicationId, managerId, payload.getRemarks());
            return ResponseEntity.ok(Map.of("message", "Leave application approved and ledger deducted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{applicationId}/reject")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_LEAVES)")
    public ResponseEntity<?> rejectLeave(@PathVariable Long applicationId, @Valid @RequestBody ActionApprovalRequest payload) {
        try {
            Long managerId = resolveManagerId(payload);
            leaveManagementService.rejectLeave(applicationId, managerId, payload.getRemarks());
            return ResponseEntity.ok(Map.of("message", "Leave application rejected."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{applicationId}/cancel")
    public ResponseEntity<?> requestCancellation(@PathVariable Long applicationId, @RequestBody Map<String, Object> payload) {
        try {
            Long staffId = SecurityUtils.getStaffId();
            if (staffId == null && payload.get("staffId") != null) {
                staffId = Long.valueOf(payload.get("staffId").toString());
            }
            String remarks = (String) payload.getOrDefault("remarks", "");

            LeaveApplication.ApplicationStatus status = leaveManagementService.requestCancellation(applicationId, staffId, remarks);
            if (status == LeaveApplication.ApplicationStatus.CANCELLED) {
                return ResponseEntity.ok(Map.of("message", "Leave cancelled successfully", "status", status.name()));
            }
            return ResponseEntity.ok(Map.of("message", "Cancellation requested successfully.", "status", status.name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{applicationId}/approve-cancellation")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_LEAVES)")
    public ResponseEntity<?> approveCancellation(@PathVariable Long applicationId, @Valid @RequestBody ActionApprovalRequest payload) {
        try {
            Long managerId = resolveManagerId(payload);
            leaveManagementService.approveCancellation(applicationId, managerId, payload.getRemarks());
            return ResponseEntity.ok(Map.of("message", "Cancellation approved, ledger refunded, and attendance reverted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{applicationId}/reject-cancellation")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_LEAVES)")
    public ResponseEntity<?> rejectCancellation(@PathVariable Long applicationId, @Valid @RequestBody ActionApprovalRequest payload) {
        try {
            Long managerId = resolveManagerId(payload);
            leaveManagementService.rejectCancellation(applicationId, managerId, payload.getRemarks());
            return ResponseEntity.ok(Map.of("message", "Cancellation request rejected."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long resolveManagerId(ActionApprovalRequest payload) {
        Long managerId = SecurityUtils.getStaffId();
        if (managerId == null && payload != null) {
            managerId = payload.getManagerId();
        }
        if (managerId == null) {
            throw new IllegalArgumentException("Manager ID is required");
        }
        return managerId;
    }
}
