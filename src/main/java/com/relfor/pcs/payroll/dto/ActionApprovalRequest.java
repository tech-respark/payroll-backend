package com.relfor.pcs.payroll.dto;

import lombok.Data;

@Data
public class ActionApprovalRequest {
    private Long managerId;
    private String remarks;
}
