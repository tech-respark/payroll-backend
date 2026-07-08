package com.relfor.pcs.payroll.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegularizationApprovalRequest {
    @NotNull(message = "Approver ID is required")
    private Long approverId;
}
