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
        try {
            return ResponseEntity.ok(leaveManagementService.getEligibleLeaveRules(staffId).stream()
                    .map(LeavePlanRule::getLeaveType)
                    .toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/applications/staff/{staffId}")
    public ResponseEntity<?> getStaffLeaveHistory(@PathVariable Long staffId) {
        try {
            return ResponseEntity.ok(leaveManagementService.getStaffLeaveHistory(staffId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/applications/pending")
    public ResponseEntity<?> getPendingLeaves(@RequestParam Long tenantId, @RequestParam Long storeId, org.springframework.security.core.Authentication authentication) {
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
            return ResponseEntity.ok(leaveManagementService.getPendingLeaves(tenantId, storeId, managerId, isHrAdmin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(@Valid @RequestBody LeaveApplicationRequest payload) {
        try {
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
        try {
            List<LeavePlanRule> eligibleRules = leaveManagementService.getEligibleLeaveRules(staffId);
            java.util.List<Map<String, Object>> balances = new java.util.ArrayList<>();
            for (LeavePlanRule rule : eligibleRules) {
                LeaveType type = rule.getLeaveType();
                Long typeId = type.getId();
                
                BigDecimal balance;
                if (tenantId != null && storeId != null) {
                    balance = leaveLedgerService.getOrAllocateBalance(tenantId, storeId, staffId, type, rule.getAnnualAllotment(), LocalDate.now());
                } else {
                    balance = leaveLedgerService.getAvailableBalance(staffId, typeId, LocalDate.now());
                }
                
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
            leaveManagementService.approveLeave(applicationId, payload.getManagerId(), payload.getRemarks());
            return ResponseEntity.ok(Map.of("message", "Leave application approved and ledger deducted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<?> rejectLeave(@PathVariable Long applicationId, @Valid @RequestBody ActionApprovalRequest payload) {
        try {
            leaveManagementService.rejectLeave(applicationId, payload.getManagerId(), payload.getRemarks());
            return ResponseEntity.ok(Map.of("message", "Leave application rejected."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{applicationId}/cancel")
    public ResponseEntity<?> requestCancellation(@PathVariable Long applicationId, @RequestBody Map<String, Object> payload) {
        try {
            Long staffId = Long.valueOf(payload.get("staffId").toString());
            String remarks = (String) payload.getOrDefault("remarks", "");
            
            leaveManagementService.requestCancellation(applicationId, staffId, remarks);
            return ResponseEntity.ok(Map.of("message", "Cancellation requested successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{applicationId}/approve-cancellation")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_LEAVES)")
    public ResponseEntity<?> approveCancellation(@PathVariable Long applicationId, @Valid @RequestBody ActionApprovalRequest payload) {
        try {
            leaveManagementService.approveCancellation(applicationId, payload.getManagerId(), payload.getRemarks());
            return ResponseEntity.ok(Map.of("message", "Cancellation approved, ledger refunded, and attendance reverted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{applicationId}/reject-cancellation")
    public ResponseEntity<?> rejectCancellation(@PathVariable Long applicationId, @Valid @RequestBody ActionApprovalRequest payload) {
        try {
            leaveManagementService.rejectCancellation(applicationId, payload.getManagerId(), payload.getRemarks());
            return ResponseEntity.ok(Map.of("message", "Cancellation request rejected."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
