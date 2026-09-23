package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "leave_plan_rules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "leave_type_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePlanRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private LeavePlan leavePlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "annual_allotment", nullable = false, precision = 5, scale = 2)
    private BigDecimal annualAllotment;

    @Column(name = "max_consecutive_days")
    private Integer maxConsecutiveDays;

    @Column(name = "proof_required_after_days")
    private Integer proofRequiredAfterDays;

    @Column(name = "allow_negative_balance", nullable = false)
    private boolean allowNegativeBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "accrual_frequency", length = 20)
    private AccrualFrequency accrualFrequency = AccrualFrequency.NONE;

    @Column(name = "max_carry_forward")
    private Integer maxCarryForward;

    public enum AccrualFrequency {
        MONTHLY, QUARTERLY, ANNUALLY, NONE
    }
}
