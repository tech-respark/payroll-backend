package com.relfor.pcs.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "commission_slab")
@Getter
@Setter
public class CommissionSlab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnore
    private CommissionPlan plan;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal thresholdFrom = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal thresholdTo;

    @Column(precision = 7, scale = 3, nullable = false)
    private BigDecimal ratePercent = BigDecimal.ZERO;
}
