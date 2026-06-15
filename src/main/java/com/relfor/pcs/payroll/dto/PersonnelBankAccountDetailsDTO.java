package com.relfor.pcs.payroll.dto;

public class PersonnelBankAccountDetailsDTO {
	private Long id;
	private Long personnelCode;
	private String bankName;
	private String bankBranch;
	private String ifscCode;
	private String accountNumber;

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankBranch() {
		return bankBranch;
	}

	public void setBankBranch(String bankBranch) {
		this.bankBranch = bankBranch;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPersonnelCode() {
		return personnelCode;
	}

	public void setPersonnelCode(Long personnelCode) {
		this.personnelCode = personnelCode;
	}

	@Override
	public String toString() {
		return "PersonnelBankAccountDetailsDTO{" +
				"id=" + id +
				", personnelCode=" + personnelCode +
				", bankName='" + bankName + '\'' +
				", bankBranch='" + bankBranch + '\'' +
				", ifscCode='" + ifscCode + '\'' +
				", accountNumber='" + accountNumber + '\'' +
				'}';
	}
}
