package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employee_leave_enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"staff_id", "plan_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLeaveEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "staff_id", nullable = false, unique = true)
    private Long staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private LeavePlan leavePlan;

    @Column(name = "enrolled_date", nullable = false)
    private LocalDate enrolledDate;
}
