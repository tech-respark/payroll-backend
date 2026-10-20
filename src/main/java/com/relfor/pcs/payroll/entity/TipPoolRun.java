package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tip_pool_run")
@Getter
@Setter
public class TipPoolRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private LocalDate periodFrom;

    @Column(nullable = false)
    private LocalDate periodTo;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal poolAmount = BigDecimal.ZERO;

    @Column(length = 20, nullable = false)
    private String method = "HOURS";

    @Column(length = 20, nullable = false)
    private String status = "DRAFT";

    @Column(length = 400)
    private String remarks;

    @OneToMany(mappedBy = "tipPool", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TipPoolShare> shares = new ArrayList<>();
}
