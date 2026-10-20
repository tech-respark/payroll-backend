package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "staff_availability", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "store_id", "staff_id", "weekday"})
})
@Getter
@Setter
public class StaffAvailability extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(nullable = false, length = 12)
    private String weekday;

    @Column(length = 8)
    private String availableFrom;

    @Column(length = 8)
    private String availableTo;

    @Column(nullable = false)
    private Boolean available = true;
}
