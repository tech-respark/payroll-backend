package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "leave_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "requested_days", nullable = false, precision = 4, scale = 2)
    private BigDecimal requestedDays; // Can support 0.5 (Half Days)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status; // PENDING, APPROVED, REJECTED, CANCELLED

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl; // For SL doctor notes

    @Column(name = "manager_remarks", length = 255)
    private String managerRemarks;

    @Version
    @Column(name = "version")
    private Long version; // Optimistic Locking

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.status = ApplicationStatus.PENDING;
    }

    public enum ApplicationStatus {
        PENDING, APPROVED, REJECTED, CANCELLATION_REQUESTED, CANCELLED
    }
}
