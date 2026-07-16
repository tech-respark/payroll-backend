package com.relfor.pcs.payroll.dto;

public class ExternalStaffDTO {
    private String externalStaffId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private Boolean isActive;
    private String roleName;

    public String getExternalStaffId() {
        return externalStaffId;
    }

    public void setExternalStaffId(String externalStaffId) {
        this.externalStaffId = externalStaffId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
