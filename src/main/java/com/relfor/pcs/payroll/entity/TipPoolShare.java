package com.relfor.pcs.payroll.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tip_pool_share")
@Getter
@Setter
public class TipPoolShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tip_pool_id", nullable = false)
    @JsonIgnore
    private TipPoolRun tipPool;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(precision = 19, scale = 2)
    private BigDecimal hoursWorked = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal points = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal shareAmount = BigDecimal.ZERO;
}
