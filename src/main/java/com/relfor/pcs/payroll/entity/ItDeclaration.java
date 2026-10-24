package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "it_declaration")
@Getter
@Setter
public class ItDeclaration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(length = 12, nullable = false)
    private String financialYear;

    @Column(length = 20)
    private String regime = "OLD";

    @Column(precision = 19, scale = 2)
    private BigDecimal section80c = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal hra = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal otherExemption = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal declaredTotal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal approvedTotal = BigDecimal.ZERO;

    @Column(length = 20, nullable = false)
    private String status = "PENDING";

    @Column(length = 400)
    private String proofNote;

    @Column(length = 80)
    private String decidedBy;
}
