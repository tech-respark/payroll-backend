package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.entity.AttendanceRegularizationRequest;
import com.relfor.pcs.payroll.service.AttendanceRegularizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/payroll-management/v1/regularizations")
@RequiredArgsConstructor
public class AttendanceRegularizationController {

    private final AttendanceRegularizationService regularizationService;

    @PostMapping("/apply")
    public ResponseEntity<?> applyForRegularization(@RequestBody Map<String, Object> payload) {
        try {
            Long staffId = Long.valueOf(payload.get("staffId").toString());
            LocalDate date = LocalDate.parse(payload.get("date").toString());
            
            LocalTime inTime = payload.containsKey("inTime") && payload.get("inTime") != null ? 
                    LocalTime.parse(payload.get("inTime").toString()) : null;
                    
            LocalTime outTime = payload.containsKey("outTime") && payload.get("outTime") != null ? 
                    LocalTime.parse(payload.get("outTime").toString()) : null;
                    
            String reason = (String) payload.get("reason");

            AttendanceRegularizationRequest request = regularizationService.requestRegularization(staffId, date, inTime, outTime, reason);
            return ResponseEntity.ok(Map.of("message", "Regularization request submitted", "requestId", request.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRegularization(@PathVariable Long requestId, @RequestBody Map<String, Object> payload) {
        try {
            Long approverId = Long.valueOf(payload.get("approverId").toString());
            regularizationService.approveRegularization(requestId, approverId);
            return ResponseEntity.ok(Map.of("message", "Regularization request approved and attendance synced."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRegularization(@PathVariable Long requestId, @RequestBody Map<String, Object> payload) {
        try {
            Long approverId = Long.valueOf(payload.get("approverId").toString());
            regularizationService.rejectRegularization(requestId, approverId);
            return ResponseEntity.ok(Map.of("message", "Regularization request rejected."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
