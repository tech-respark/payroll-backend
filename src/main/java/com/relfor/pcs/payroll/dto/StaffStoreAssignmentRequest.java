package com.relfor.pcs.payroll.dto;

import java.util.List;

public class StaffStoreAssignmentRequest {
    private List<Long> storeIds;
    private Long roleId;

    public List<Long> getStoreIds() {
        return storeIds;
    }

    public void setStoreIds(List<Long> storeIds) {
        this.storeIds = storeIds;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
