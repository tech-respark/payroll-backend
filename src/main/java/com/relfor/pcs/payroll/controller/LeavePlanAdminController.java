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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.relfor.pcs.payroll.entity.EmployeeLeaveEnrollment;
import com.relfor.pcs.payroll.repository.EmployeeLeaveEnrollmentRepository;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/payroll-management/v1/admin/leave-plans")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_LEAVES)")
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
            if (leaveType.getLocationId() == null) {
                leaveType.setLocationId(leaveType.getStoreId());
            }
            LeaveType saved = leaveTypeRepository.save(leaveType);
            return ResponseEntity.ok(Map.of("message", "Leave Type created", "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/leave-types/{id}")
    public ResponseEntity<?> updateLeaveType(@PathVariable Long id, @RequestBody LeaveType leaveTypePayload) {
        try {
            LeaveType existing = leaveTypeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Leave Type not found"));
            
            existing.setLeaveCode(leaveTypePayload.getLeaveCode());
            existing.setLeaveName(leaveTypePayload.getLeaveName());
            existing.setPaid(leaveTypePayload.isPaid());
            
            LeaveType saved = leaveTypeRepository.save(existing);
            return ResponseEntity.ok(Map.of("message", "Leave Type updated", "data", saved));
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
            if (leavePlan.getLocationId() == null) {
                leavePlan.setLocationId(leavePlan.getStoreId());
            }
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
            
            Optional<EmployeeLeaveEnrollment> existingOpt = enrollmentRepository.findFirstByStaffIdOrderByEnrolledDateDesc(staffId);
            
            EmployeeLeaveEnrollment enrollment;
            if (existingOpt.isPresent()) {
                enrollment = existingOpt.get();
                enrollment.setLeavePlan(plan);
                enrollment.setEnrolledDate(LocalDate.now());
            } else {
                enrollment = EmployeeLeaveEnrollment.builder()
                        .tenantId(tenantId)
                        .storeId(storeId)
                        .locationId(storeId)
                        .staffId(staffId)
                        .leavePlan(plan)
                        .enrolledDate(LocalDate.now())
                        .build();
            }
                    
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

    @GetMapping("/plans/{planId}/rules")
    public ResponseEntity<?> getRulesForPlan(@PathVariable Long planId, @RequestParam Long tenantId, @RequestParam Long storeId) {
        List<LeavePlanRule> rules = ruleRepository.findByLeavePlan_Id(planId);
        List<Map<String, Object>> result = rules.stream().map(r -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", r.getId());
            map.put("leaveTypeId", r.getLeaveType().getId());
            map.put("annualAllotment", r.getAnnualAllotment());
            map.put("maxConsecutiveDays", r.getMaxConsecutiveDays());
            map.put("proofRequiredAfterDays", r.getProofRequiredAfterDays());
            map.put("allowNegativeBalance", r.isAllowNegativeBalance());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/rules/{ruleId}/delete")
    public ResponseEntity<?> deleteRule(@PathVariable Long ruleId) {
        try {
            ruleRepository.deleteById(ruleId);
            return ResponseEntity.ok(Map.of("message", "Rule deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/plans/{planId}/update")
    public ResponseEntity<?> updatePlan(@PathVariable Long planId, @RequestBody Map<String, Object> payload) {
        try {
            LeavePlan plan = leavePlanRepository.findById(planId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Leave Plan ID"));
            
            if (payload.containsKey("planName")) {
                plan.setPlanName(payload.get("planName").toString());
            }
            if (payload.containsKey("effectiveYear")) {
                plan.setEffectiveYear(Integer.valueOf(payload.get("effectiveYear").toString()));
            }
            leavePlanRepository.save(plan);
            return ResponseEntity.ok(Map.of("message", "Plan updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/enrollments")
    public ResponseEntity<?> getEnrollments(@RequestParam Long tenantId, @RequestParam Long storeId) {
        List<EmployeeLeaveEnrollment> enrollments = enrollmentRepository.findByTenantIdAndStoreId(tenantId, storeId);
        List<Map<String, Object>> result = enrollments.stream().map(e -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", e.getId());
            map.put("staffId", e.getStaffId());
            map.put("planId", e.getLeavePlan().getId());
            map.put("enrolledDate", e.getEnrolledDate());
            map.put("status", "ACTIVE");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
