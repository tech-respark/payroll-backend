package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "reimbursement_claim")
@Getter
@Setter
public class ReimbursementClaim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(length = 40, nullable = false)
    private String category = "OTHER";

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 20, nullable = false)
    private String salaryMonth;

    @Column(nullable = false)
    private Integer salaryYear;

    @Column(length = 20, nullable = false)
    private String status = "PENDING";

    @Column(length = 400)
    private String receiptNote;

    @Column(length = 500)
    private String remarks;

    @Column(length = 80)
    private String decidedBy;
}
