package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@Entity
@Table(name = "personnel_bank_account_details")
public class PersonnelBankAccountDetails extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	private Long personnelCode;
	private String bankName;
	private String bankBranch;
	private String ifscCode;
	private String accountNumber;
	@OneToOne
	@JoinColumn(name = "personnel_details_id", referencedColumnName = "id", unique = true)
	private PersonnelDetails personnelDetails;
	// Getters and Setters
	public Long getId() { return id; }

	public void setId(Long id) { this.id = id; }

	public Long getPersonnelCode() { return personnelCode; }

	public void setPersonnelCode(Long personnelCode) { this.personnelCode = personnelCode; }

	public String getBankName() { return bankName; }

	public void setBankName(String bankName) { this.bankName = bankName; }

	public String getBankBranch() { return bankBranch; }

	public void setBankBranch(String bankBranch) { this.bankBranch = bankBranch; }

	public String getIfscCode() { return ifscCode; }

	public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

	public String getAccountNumber() { return accountNumber; }

	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

	public PersonnelDetails getPersonnelDetails() {
		return personnelDetails;
	}

	public void setPersonnelDetails(PersonnelDetails personnelDetails) {
		this.personnelDetails = personnelDetails;
	}
}