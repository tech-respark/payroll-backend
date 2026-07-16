package com.relfor.pcs.payroll.dto;

import java.util.List;

public class ExternalRolesResponse {
    private List<ExternalRoleDTO> roles;

    public List<ExternalRoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<ExternalRoleDTO> roles) {
        this.roles = roles;
    }
}
