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

@RestController
@RequestMapping("/payroll-management/v1/admin/leave-plans")
@RequiredArgsConstructor
public class LeavePlanAdminController {

    private final LeavePlanRepository leavePlanRepository;
    private final LeavePlanRuleRepository ruleRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    @PostMapping("/map-rules")
    public ResponseEntity<?> mapLeavePlanRule(@RequestBody Map<String, Object> payload) {
        try {
            Long planId = Long.valueOf(payload.get("planId").toString());
            Long leaveTypeId = Long.valueOf(payload.get("leaveTypeId").toString());
            BigDecimal annualAllotment = new BigDecimal(payload.get("annualAllotment").toString());
            Integer maxConsecutiveDays = payload.containsKey("maxConsecutiveDays") ? Integer.valueOf(payload.get("maxConsecutiveDays").toString()) : null;
            Integer proofRequiredAfterDays = payload.containsKey("proofRequiredAfterDays") ? Integer.valueOf(payload.get("proofRequiredAfterDays").toString()) : null;
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
