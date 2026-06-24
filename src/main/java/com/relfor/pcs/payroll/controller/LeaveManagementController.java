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

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLeave(@RequestBody Map<String, Object> payload) {
        try {
            Long personnelId = Long.valueOf(payload.get("personnelId").toString());
            Long leaveTypeId = Long.valueOf(payload.get("leaveTypeId").toString());
            LocalDate startDate = LocalDate.parse(payload.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(payload.get("endDate").toString());
            String reason = (String) payload.get("reason");
            String attachmentUrl = (String) payload.get("attachmentUrl");

            LeaveType type = leaveTypeRepository.findById(leaveTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Leave Type ID"));

            LeaveApplication application = leaveManagementService.applyForLeave(personnelId, type, startDate, endDate, reason, attachmentUrl);
            return ResponseEntity.ok(Map.of("message", "Leave application submitted successfully", "applicationId", application.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/balances/{personnelId}")
    public ResponseEntity<?> getAvailableBalances(@PathVariable Long personnelId, @RequestParam Long leaveTypeId) {
        try {
            BigDecimal balance = leaveLedgerService.getAvailableBalance(personnelId, leaveTypeId, LocalDate.now());
            return ResponseEntity.ok(Map.of("personnelId", personnelId, "leaveTypeId", leaveTypeId, "availableBalance", balance));
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
    public ResponseEntity<?> cancelLeave(@PathVariable Long applicationId) {
        try {
            leaveManagementService.cancelLeave(applicationId);
            return ResponseEntity.ok(Map.of("message", "Leave application cancelled and ledger refunded if applicable."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
