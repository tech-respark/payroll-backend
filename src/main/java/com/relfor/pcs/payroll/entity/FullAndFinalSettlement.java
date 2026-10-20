package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "full_and_final_settlement")
@Getter
@Setter
public class FullAndFinalSettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(nullable = false)
    private LocalDate lastWorkingDate;

    @Column(length = 20, nullable = false)
    private String status = "DRAFT";

    @Column(precision = 19, scale = 2)
    private BigDecimal unpaidSalary = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal leaveEncashment = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal advanceRecovery = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal netPayable = BigDecimal.ZERO;

    @Column(length = 500)
    private String remarks;
}
