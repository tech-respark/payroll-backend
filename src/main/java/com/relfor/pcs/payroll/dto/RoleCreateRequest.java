package com.relfor.pcs.payroll.dto;

import lombok.Data;

@Data
public class RoleCreateRequest {
    private String name;
    private String description;
    private Long tenantId; // Can be extracted from JWT or passed in request
    private java.util.List<String> permissions;
}
