package com.relfor.pcs.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveApplicationRequest {
    @NotNull(message = "Staff ID is required")
    private Long staffId;
    
    @NotNull(message = "Tenant ID is required")
    private Long tenantId;
    
    @NotNull(message = "Store ID is required")
    private Long storeId;
    
    @NotNull(message = "Leave Type ID is required")
    private Long leaveTypeId;
    
    @NotNull(message = "Start Date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End Date is required")
    private LocalDate endDate;
    
    private String reason;
    private String attachmentUrl;
    private String leaveSession = "FULL_DAY";
}
