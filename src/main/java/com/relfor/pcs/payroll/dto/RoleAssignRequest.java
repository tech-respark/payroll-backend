package com.relfor.pcs.payroll.dto;

import lombok.Data;

@Data
public class RoleAssignRequest {
    private Long staffId;
    private Long roleId;
    private Long storeId; // Needed for assignment
    private Long tenantId; // Can be extracted from JWT
}
