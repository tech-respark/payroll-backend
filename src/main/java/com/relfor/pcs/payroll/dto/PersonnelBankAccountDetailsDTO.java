package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PersonnelBankAccountDetailsDTO {
	private Long id;
	private Long staffId;
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

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	@Override
	public String toString() {
		return "PersonnelBankAccountDetailsDTO{" +
				"id=" + id +
				", staffId=" + staffId +
				", bankName='" + bankName + '\'' +
				", bankBranch='" + bankBranch + '\'' +
				", ifscCode='" + ifscCode + '\'' +
				", accountNumber='" + accountNumber + '\'' +
				'}';
	}
}
