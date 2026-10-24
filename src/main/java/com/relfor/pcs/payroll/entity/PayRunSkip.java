package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pay_run_skip", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"pay_run_id", "staff_id"})
})
@Getter
@Setter
public class PayRunSkip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pay_run_id", nullable = false)
    private Long payRunId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(length = 240)
    private String reason;
}
