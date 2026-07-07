package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "leave_application_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveApplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_application_id", nullable = false)
    private LeaveApplication leaveApplication;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private LogAction action;

    @Column(name = "actor_id", nullable = false)
    private Long actorId; // ID of the person performing the action (staff or manager)

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public enum LogAction {
        APPLIED, APPROVED, REJECTED, CANCELLATION_REQUESTED, CANCELLED, CANCELLATION_REJECTED
    }
}
