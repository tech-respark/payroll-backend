package com.relfor.pcs.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActionApprovalRequest {
    @NotNull(message = "Manager ID is required")
    private Long managerId;
    
    private String remarks;
}
