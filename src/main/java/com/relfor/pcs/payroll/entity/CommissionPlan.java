package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commission_plan")
@Getter
@Setter
public class CommissionPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    private Long staffId;

    @Column(nullable = false, length = 80)
    private String planName;

    @Column(nullable = false, length = 20)
    private String category = "SERVICE";

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommissionSlab> slabs = new ArrayList<>();
}
