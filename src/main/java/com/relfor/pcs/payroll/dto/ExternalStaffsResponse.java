package com.relfor.pcs.payroll.dto;

import java.util.List;

public class ExternalStaffsResponse {
    private List<ExternalStaffDTO> staff;

    public List<ExternalStaffDTO> getStaff() {
        return staff;
    }

    public void setStaff(List<ExternalStaffDTO> staff) {
        this.staff = staff;
    }
}
