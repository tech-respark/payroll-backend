package com.relfor.pcs.payroll.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "store_profile_config")
public class StoreProfileConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private Long tenantId;
    private Long storeId;
    
    // Billing & Profile Fields
    private String address;
    private String email;
    private String phone;
    private String phone1;
    private String gstNo;
    private String vatNo;
    private String crNo;
    private String companyName;
    private String logoPath;
    private String countryCode;
    
    // Footers
    private String billFooter;
    private String giftCardFooter;
    private String membershipFooter;
    private String packageFooter;
    private String advanceFooter;
    private String balanceFooter;
    
    // Configuration Fields
    private Integer defaultSlotTime = 30;
    private Boolean stylistProductivity = false;
    private Double workingDaysPerMonth = 30D;
    private Double workingHoursPerDay = 9.5;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getGstNo() {
        return gstNo;
    }

    public void setGstNo(String gstNo) {
        this.gstNo = gstNo;
    }

    public String getVatNo() {
        return vatNo;
    }

    public void setVatNo(String vatNo) {
        this.vatNo = vatNo;
    }

    public String getCrNo() {
        return crNo;
    }

    public void setCrNo(String crNo) {
        this.crNo = crNo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getBillFooter() {
        return billFooter;
    }

    public void setBillFooter(String billFooter) {
        this.billFooter = billFooter;
    }

    public String getGiftCardFooter() {
        return giftCardFooter;
    }

    public void setGiftCardFooter(String giftCardFooter) {
        this.giftCardFooter = giftCardFooter;
    }

    public String getMembershipFooter() {
        return membershipFooter;
    }

    public void setMembershipFooter(String membershipFooter) {
        this.membershipFooter = membershipFooter;
    }

    public String getPackageFooter() {
        return packageFooter;
    }

    public void setPackageFooter(String packageFooter) {
        this.packageFooter = packageFooter;
    }

    public String getAdvanceFooter() {
        return advanceFooter;
    }

    public void setAdvanceFooter(String advanceFooter) {
        this.advanceFooter = advanceFooter;
    }

    public String getBalanceFooter() {
        return balanceFooter;
    }

    public void setBalanceFooter(String balanceFooter) {
        this.balanceFooter = balanceFooter;
    }

    public Integer getDefaultSlotTime() {
        return defaultSlotTime;
    }

    public void setDefaultSlotTime(Integer defaultSlotTime) {
        this.defaultSlotTime = defaultSlotTime;
    }

    public Boolean getStylistProductivity() {
        return stylistProductivity;
    }

    public void setStylistProductivity(Boolean stylistProductivity) {
        this.stylistProductivity = stylistProductivity;
    }

    public Double getWorkingDaysPerMonth() {
        return workingDaysPerMonth;
    }

    public void setWorkingDaysPerMonth(Double workingDaysPerMonth) {
        this.workingDaysPerMonth = workingDaysPerMonth;
    }

    public Double getWorkingHoursPerDay() {
        return workingHoursPerDay;
    }

    public void setWorkingHoursPerDay(Double workingHoursPerDay) {
        this.workingHoursPerDay = workingHoursPerDay;
    }
}
