package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "salary_revision")
@Getter
@Setter
public class SalaryRevision extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(length = 20, nullable = false)
    private String effectiveMonth;

    @Column(nullable = false)
    private Integer effectiveYear;

    @Column(precision = 19, scale = 2)
    private BigDecimal previousAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal newAmount = BigDecimal.ZERO;

    @Column(length = 400)
    private String notes;
}
