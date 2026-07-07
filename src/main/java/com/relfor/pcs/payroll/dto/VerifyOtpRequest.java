package com.relfor.pcs.payroll.dto;

public class VerifyOtpRequest {
    private String usernameOrMobile;
    private String otp;

    public String getUsernameOrMobile() {
        return usernameOrMobile;
    }
    public void setUsernameOrMobile(String usernameOrMobile) {
        this.usernameOrMobile = usernameOrMobile;
    }
    public String getOtp() {
        return otp;
    }
    public void setOtp(String otp) {
        this.otp = otp;
    }
}
