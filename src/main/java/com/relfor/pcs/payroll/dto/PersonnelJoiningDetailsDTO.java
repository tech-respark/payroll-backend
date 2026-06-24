package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PersonnelJoiningDetailsDTO {
	private Long staffId;
	private String employeeCode;
	private Long reportingTo;
	private Long storeId;
	private String uanNumber;
	private Float workingHours;

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public Long getReportingTo() {
		return reportingTo;
	}

	public void setReportingTo(Long reportingTo) {
		this.reportingTo = reportingTo;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public String getUanNumber() {
		return uanNumber;
	}

	public void setUanNumber(String uanNumber) {
		this.uanNumber = uanNumber;
	}

	public Float getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(Float workingHours) {
		this.workingHours = workingHours;
	}

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	@Override
	public String toString() {
		return "PersonnelJoiningDetailsDTO{" +
				"staffId=" + staffId +
				", employeeCode='" + employeeCode + '\'' +
				", reportingTo=" + reportingTo +
				", storeId=" + storeId +
				", uanNumber='" + uanNumber + '\'' +
				", workingHours=" + workingHours +
				'}';
	}
}
