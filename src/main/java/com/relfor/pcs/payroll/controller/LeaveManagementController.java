package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.entity.LeaveApplication;
import com.relfor.pcs.payroll.entity.LeaveType;
import com.relfor.pcs.payroll.repository.LeaveTypeRepository;
import com.relfor.pcs.payroll.service.LeaveLedgerService;
import com.relfor.pcs.payroll.service.LeaveManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
                    .map(com.relfor.pcs.payroll.entity.LeavePlanRule::getLeaveType)
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
    public ResponseEntity<?> getPendingLeaves(@RequestParam Long tenantId, @RequestParam Long storeId) {
        try {
            return ResponseEntity.ok(leaveManagementService.getPendingLeaves(tenantId, storeId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(@RequestBody Map<String, Object> payload) {
        try {
            Long staffId = Long.valueOf(payload.get("staffId").toString());
            Long tenantId = Long.valueOf(payload.get("tenantId").toString());
            Long storeId = Long.valueOf(payload.get("storeId").toString());
            Long leaveTypeId = Long.valueOf(payload.get("leaveTypeId").toString());
            LocalDate startDate = LocalDate.parse(payload.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(payload.get("endDate").toString());
            String reason = (String) payload.get("reason");
            String attachmentUrl = (String) payload.get("attachmentUrl");

            LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Leave Type ID"));

            LeaveApplication application = leaveManagementService.applyForLeave(tenantId, storeId, staffId, leaveType, startDate, endDate, reason, attachmentUrl);
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
            java.util.List<com.relfor.pcs.payroll.entity.LeavePlanRule> eligibleRules = leaveManagementService.getEligibleLeaveRules(staffId);
            java.util.List<Map<String, Object>> balances = new java.util.ArrayList<>();
            for (com.relfor.pcs.payroll.entity.LeavePlanRule rule : eligibleRules) {
                com.relfor.pcs.payroll.entity.LeaveType type = rule.getLeaveType();
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
    public ResponseEntity<?> approveLeave(@PathVariable Long applicationId, @RequestBody Map<String, Object> payload) {
        try {
            Long managerId = Long.valueOf(payload.get("managerId").toString());
            String remarks = (String) payload.getOrDefault("remarks", "");
            
            leaveManagementService.approveLeave(applicationId, managerId, remarks);
            return ResponseEntity.ok(Map.of("message", "Leave application approved and ledger deducted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<?> rejectLeave(@PathVariable Long applicationId, @RequestBody Map<String, Object> payload) {
        try {
            Long managerId = Long.valueOf(payload.get("managerId").toString());
            String remarks = (String) payload.get("remarks");
            
            leaveManagementService.rejectLeave(applicationId, managerId, remarks);
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
    public ResponseEntity<?> approveCancellation(@PathVariable Long applicationId, @RequestBody Map<String, Object> payload) {
        try {
            Long managerId = Long.valueOf(payload.get("managerId").toString());
            String remarks = (String) payload.getOrDefault("remarks", "");
            
            leaveManagementService.approveCancellation(applicationId, managerId, remarks);
            return ResponseEntity.ok(Map.of("message", "Cancellation approved, ledger refunded, and attendance reverted."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
