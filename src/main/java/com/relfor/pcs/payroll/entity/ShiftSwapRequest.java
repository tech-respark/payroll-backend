package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "shift_swap_request")
@Getter
@Setter
public class ShiftSwapRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long requesterStaffId;

    @Column(nullable = false)
    private Long targetStaffId;

    @Column(nullable = false)
    private LocalDate shiftDate;

    @Column(length = 20, nullable = false)
    private String status = "PENDING";

    @Column(length = 400)
    private String reason;

    private Long decidedBy;
}
