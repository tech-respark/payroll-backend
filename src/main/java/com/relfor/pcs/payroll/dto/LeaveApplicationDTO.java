package com.relfor.pcs.payroll.dto;

import com.relfor.pcs.payroll.entity.LeaveApplication;
import com.relfor.pcs.payroll.entity.LeaveType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class LeaveApplicationDTO {
    private Long id;
    private Long tenantId;
    private Long storeId;
    private Long staffId;
    private String staffName;
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal requestedDays;
    private String leaveSession;
    private LeaveApplication.ApplicationStatus status;
    private String reason;
    private String attachmentUrl;
    private String managerRemarks;
    private Instant createdAt;

    public LeaveApplicationDTO(LeaveApplication entity, String staffName) {
        this.id = entity.getId();
        this.tenantId = entity.getTenantId();
        this.storeId = entity.getStoreId();
        this.staffId = entity.getStaffId();
        this.staffName = staffName;
        this.leaveType = entity.getLeaveType();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.requestedDays = entity.getRequestedDays();
        this.leaveSession = entity.getLeaveSession();
        this.status = entity.getStatus();
        this.reason = entity.getReason();
        this.attachmentUrl = entity.getAttachmentUrl();
        this.managerRemarks = entity.getManagerRemarks();
        this.createdAt = entity.getCreatedAt();
    }
}