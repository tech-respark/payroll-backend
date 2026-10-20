package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "statutory_config", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "store_id"})
})
@Getter
@Setter
public class StatutoryConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    private Boolean pfEnabled = true;
    private Boolean esiEnabled = true;
    private Boolean ptEnabled = true;
    private Boolean tdsEnabled = false;
    private Boolean lwfEnabled = false;

    @Column(precision = 19, scale = 2)
    private BigDecimal pfWageCeiling = new BigDecimal("15000.00");
    @Column(precision = 19, scale = 2)
    private BigDecimal esiWageCeiling = new BigDecimal("21000.00");
    @Column(precision = 7, scale = 4)
    private BigDecimal pfEmployeeRate = new BigDecimal("12.0000");
    @Column(precision = 7, scale = 4)
    private BigDecimal pfEmployerRate = new BigDecimal("12.0000");
    @Column(precision = 7, scale = 4)
    private BigDecimal esiEmployeeRate = new BigDecimal("0.7500");
    @Column(precision = 7, scale = 4)
    private BigDecimal esiEmployerRate = new BigDecimal("3.2500");
    @Column(precision = 19, scale = 2)
    private BigDecimal ptAmount = new BigDecimal("200.00");
    @Column(precision = 19, scale = 2)
    private BigDecimal lwfEmployeeAmount = new BigDecimal("10.00");
    @Column(precision = 19, scale = 2)
    private BigDecimal lwfEmployerAmount = new BigDecimal("20.00");
    @Column(precision = 7, scale = 4)
    private BigDecimal tdsRate = BigDecimal.ZERO;
}
