package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_rule_overrides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRuleOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_plan_rule_id", nullable = false)
    private LeavePlanRule leavePlanRule;

    @Column(name = "max_carry_forward_override")
    private Integer maxCarryForwardOverride;
}
