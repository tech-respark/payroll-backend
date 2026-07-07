package com.relfor.pcs.payroll.dto;

public class GenerateOtpRequest {
    private String usernameOrMobile;

    public String getUsernameOrMobile() {
        return usernameOrMobile;
    }
    public void setUsernameOrMobile(String usernameOrMobile) {
        this.usernameOrMobile = usernameOrMobile;
    }
}
