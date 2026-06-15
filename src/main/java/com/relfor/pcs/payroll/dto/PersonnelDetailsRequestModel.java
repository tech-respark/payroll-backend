package com.relfor.pcs.payroll.dto;

public class PersonnelDetailsRequestModel {
	private Long id;
	private Long personnelCode;
	private String firstName;
	private String lastName;
	private String gender;
	private String designation;
	private String personnelMobileNumber;
	private String applicationName;
	private Long applicationTenantId;
	private Boolean active;

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getApplicationTenantId() {
		return applicationTenantId;
	}

	public void setApplicationTenantId(Long applicationTenantId) {
		this.applicationTenantId = applicationTenantId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getPersonnelMobileNumber() {
		return personnelMobileNumber;
	}

	public void setPersonnelMobileNumber(String personnelMobileNumber) {
		this.personnelMobileNumber = personnelMobileNumber;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public Long getPersonnelCode() {
		return personnelCode;
	}

	public void setPersonnelCode(Long personnelCode) {
		this.personnelCode = personnelCode;
	}

	@Override
	public String toString() {
		return "PersonnelDetailsRequestModel{" +
				"id=" + id +
				", personnelCode=" + personnelCode +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", gender='" + gender + '\'' +
				", designation='" + designation + '\'' +
				", personnelMobileNumber='" + personnelMobileNumber + '\'' +
				", applicationName='" + applicationName + '\'' +
				", applicationTenantId=" + applicationTenantId +
				", active=" + active +
				'}';
	}
}