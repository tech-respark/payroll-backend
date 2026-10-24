package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "pay_run", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "store_id", "salary_month", "salary_year", "cycle_type"})
})
@Getter
@Setter
public class PayRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "salary_month", length = 20, nullable = false)
    private String salaryMonth;

    @Column(name = "salary_year", nullable = false)
    private Integer salaryYear;

    @Column(name = "cycle_type", length = 20, nullable = false)
    private String cycleType = "REGULAR";

    @Column(length = 30, nullable = false)
    private String status = "DRAFT";

    private Integer staffCount = 0;
    private Integer calculatedCount = 0;
    private Integer skippedCount = 0;

    @Column(length = 80)
    private String submittedBy;
    @Column(length = 80)
    private String approvedBy;
    @Column(length = 80)
    private String publishedBy;

    private Instant calculatedAt;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant publishedAt;
    private Instant lockedAt;

    @Column(length = 500)
    private String notes;
}
