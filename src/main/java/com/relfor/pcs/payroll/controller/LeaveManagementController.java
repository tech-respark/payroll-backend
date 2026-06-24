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
            Long staffId = Long.valueOf(payload.get("staffId").toString());
            Long leaveTypeId = Long.valueOf(payload.get("leaveTypeId").toString());
            LocalDate startDate = LocalDate.parse(payload.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(payload.get("endDate").toString());
            String reason = (String) payload.get("reason");
            String attachmentUrl = (String) payload.get("attachmentUrl");

            LeaveType type = leaveTypeRepository.findById(leaveTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Leave Type ID"));

            LeaveApplication application = leaveManagementService.applyForLeave(staffId, type, startDate, endDate, reason, attachmentUrl);
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
