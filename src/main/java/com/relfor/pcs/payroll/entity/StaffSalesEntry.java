package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "staff_sales_entry")
@Getter
@Setter
public class StaffSalesEntry extends BaseEntity {

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
    private LocalDate saleDate;

    @Column(nullable = false, length = 20)
    private String category = "SERVICE";

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 20)
    private String source = "MANUAL";

    @Column(length = 80)
    private String externalRef;

    @Column(length = 40)
    private String jobCode;

    @Column(length = 400)
    private String notes;
}
