package com.relfor.pcs.payroll.dto;

public class PrintBillDetailsDTO {
	private long storeId;
    private long tenantId;
    private String tenantName;
    private String storeName;
    private String email;
    private String phone;
    private String phone1;
    private String address;
    private String gstNo;
    private String vatNo;
    private String crNo;
    private int active;
    private String companyName;
    private String logoPath;
    private String countryCode;
	private String billFooter;
	private String giftCardFooter;
	private String membershipFooter;
	private String packageFooter;
	private String advanceFooter;
	private String balanceFooter;

	public PrintBillDetailsDTO() {
		super();
	}

	public PrintBillDetailsDTO(long storeId, long tenantId, String tenantName, String storeName, String email,
			String phone, String phone1, String address, String gstNo, String vatNo, String crNo, int active,
			String logoPath, String countryCode, String companyName, String billFooter, String giftCardFooter,
			String membershipFooter, String packageFooter, String advanceFooter, String balanceFooter) {
		super();
		this.storeId = storeId;
		this.tenantId = tenantId;
		this.tenantName = tenantName;
		this.storeName = storeName;
		this.email = email;
		this.phone = phone;
		this.phone1 = phone1;
		this.address = address;
		this.gstNo = gstNo;
		this.vatNo = vatNo;
		this.crNo = crNo;
		this.active = active;
		this.logoPath = logoPath;
		this.countryCode = countryCode;
		this.companyName = companyName;
		this.billFooter = billFooter;
		this.giftCardFooter = giftCardFooter;
		this.membershipFooter = membershipFooter;
		this.packageFooter = packageFooter;
		this.advanceFooter = advanceFooter;
		this.balanceFooter = balanceFooter;
	}
    
	public long getStoreId() {
		return storeId;
	}
	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}
	public long getTenantId() {
		return tenantId;
	}
	public void setTenantId(long tenantId) {
		this.tenantId = tenantId;
	}
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
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
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
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
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
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
	public String getGstNo() {
		return gstNo;
	}
	public void setGstNo(String gstNo) {
		this.gstNo = gstNo;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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
}
