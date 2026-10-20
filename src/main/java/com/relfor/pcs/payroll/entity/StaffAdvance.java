package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "staff_advance")
@Getter
@Setter
public class StaffAdvance extends BaseEntity {

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
    private String advanceType = "ADVANCE";

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal principal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal installmentAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    private Integer remainingInstallments;

    @Column(length = 20, nullable = false)
    private String status = "ACTIVE";

    @Column(length = 20)
    private String startMonth;

    private Integer startYear;

    @Column(length = 20)
    private String lastRecoveredMonth;

    private Integer lastRecoveredYear;

    @Column(precision = 19, scale = 2)
    private BigDecimal lastRecoveredAmount = BigDecimal.ZERO;

    private LocalDate issuedOn;

    @Column(length = 500)
    private String remarks;
}
