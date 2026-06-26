package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.entity.LeavePlan;
import com.relfor.pcs.payroll.entity.LeavePlanRule;
import com.relfor.pcs.payroll.entity.LeaveType;
import com.relfor.pcs.payroll.repository.LeavePlanRepository;
import com.relfor.pcs.payroll.repository.LeavePlanRuleRepository;
import com.relfor.pcs.payroll.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

import com.relfor.pcs.payroll.entity.EmployeeLeaveEnrollment;
import com.relfor.pcs.payroll.repository.EmployeeLeaveEnrollmentRepository;
import java.time.LocalDate;

@RestController
@RequestMapping("/payroll-management/v1/admin/leave-plans")
@RequiredArgsConstructor
public class LeavePlanAdminController {

    private final LeavePlanRepository leavePlanRepository;
    private final LeavePlanRuleRepository ruleRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeLeaveEnrollmentRepository enrollmentRepository;

    @GetMapping("/leave-types")
    public ResponseEntity<?> getAllLeaveTypes(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ResponseEntity.ok(leaveTypeRepository.findByTenantIdAndStoreId(tenantId, storeId));
    }

    @PostMapping("/leave-types")
    public ResponseEntity<?> createLeaveType(@RequestBody LeaveType leaveType) {
        try {
            LeaveType saved = leaveTypeRepository.save(leaveType);
            return ResponseEntity.ok(Map.of("message", "Leave Type created", "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/plans")
    public ResponseEntity<?> getAllLeavePlans(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ResponseEntity.ok(leavePlanRepository.findByTenantIdAndStoreId(tenantId, storeId));
    }

    @PostMapping("/plans")
    public ResponseEntity<?> createLeavePlan(@RequestBody LeavePlan leavePlan) {
        try {
            LeavePlan saved = leavePlanRepository.save(leavePlan);
            return ResponseEntity.ok(Map.of("message", "Leave Plan created", "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/plans/{planId}/assign/{staffId}")
    public ResponseEntity<?> assignPlanToStaff(@PathVariable Long planId, @PathVariable Long staffId, @RequestParam Long tenantId, @RequestParam Long storeId) {
        try {
            LeavePlan plan = leavePlanRepository.findById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Leave Plan ID"));
            
            EmployeeLeaveEnrollment enrollment = EmployeeLeaveEnrollment.builder()
                    .tenantId(tenantId)
                    .storeId(storeId)
                    .staffId(staffId)
                    .leavePlan(plan)
                    .enrolledDate(LocalDate.now())
                    .build();
                    
            enrollmentRepository.save(enrollment);
            return ResponseEntity.ok(Map.of("message", "Staff successfully enrolled in Leave Plan"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/map-rules")
    public ResponseEntity<?> mapLeavePlanRule(@RequestBody Map<String, Object> payload) {
        try {
            Long planId = Long.valueOf(payload.get("planId").toString());
            Long leaveTypeId = Long.valueOf(payload.get("leaveTypeId").toString());
            BigDecimal annualAllotment = new BigDecimal(payload.get("annualAllotment").toString());
            Integer maxConsecutiveDays = payload.containsKey("maxConsecutiveDays") && payload.get("maxConsecutiveDays") != null && !payload.get("maxConsecutiveDays").toString().isEmpty() ? Integer.valueOf(payload.get("maxConsecutiveDays").toString()) : null;
            Integer proofRequiredAfterDays = payload.containsKey("proofRequiredAfterDays") && payload.get("proofRequiredAfterDays") != null && !payload.get("proofRequiredAfterDays").toString().isEmpty() ? Integer.valueOf(payload.get("proofRequiredAfterDays").toString()) : null;
            Boolean allowNegativeBalance = Boolean.valueOf(payload.getOrDefault("allowNegativeBalance", "false").toString());

            LeavePlan plan = leavePlanRepository.findById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Leave Plan ID"));

            LeaveType type = leaveTypeRepository.findById(leaveTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Leave Type ID"));

            LeavePlanRule rule = LeavePlanRule.builder()
                    .leavePlan(plan)
                    .leaveType(type)
                    .annualAllotment(annualAllotment)
                    .maxConsecutiveDays(maxConsecutiveDays)
                    .proofRequiredAfterDays(proofRequiredAfterDays)
                    .allowNegativeBalance(allowNegativeBalance)
                    .build();

            ruleRepository.save(rule);
            return ResponseEntity.ok(Map.of("message", "Rule successfully mapped to Leave Plan", "ruleId", rule.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
