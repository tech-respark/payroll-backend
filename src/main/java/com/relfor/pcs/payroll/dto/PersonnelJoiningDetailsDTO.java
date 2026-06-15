package com.relfor.pcs.payroll.dto;

public class PersonnelJoiningDetailsDTO {
	private Long personnelCode;
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

	public Long getPersonnelCode() {
		return personnelCode;
	}

	public void setPersonnelCode(Long personnelCode) {
		this.personnelCode = personnelCode;
	}

	@Override
	public String toString() {
		return "PersonnelJoiningDetailsDTO{" +
				"personnelCode=" + personnelCode +
				", employeeCode='" + employeeCode + '\'' +
				", reportingTo=" + reportingTo +
				", storeId=" + storeId +
				", uanNumber='" + uanNumber + '\'' +
				", workingHours=" + workingHours +
				'}';
	}
}
