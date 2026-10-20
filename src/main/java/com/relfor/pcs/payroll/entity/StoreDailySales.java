package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "store_daily_sales", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "store_id", "sale_date"})
})
@Getter
@Setter
public class StoreDailySales extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal netSales = BigDecimal.ZERO;

    private Integer covers;

    @Column(length = 400)
    private String notes;
}
