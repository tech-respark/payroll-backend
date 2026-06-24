package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;

@Entity
@Table(name = "attendance_regularization_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRegularizationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "date_to_regularize", nullable = false)
    private LocalDate dateToRegularize;

    @Column(name = "requested_in_time")
    private LocalTime requestedInTime;

    @Column(name = "requested_out_time")
    private LocalTime requestedOutTime;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RegularizationStatus status;

    @Column(name = "approver_id")
    private Long approverId; // Manager's PersonnelDetails.id (reporting_to)

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.status = RegularizationStatus.PENDING;
    }

    public enum RegularizationStatus {
        PENDING, APPROVED, REJECTED
    }
}
