package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PersonnelJoiningDetailsDTO {
	private Long staffId;
	private String employeeCode;
	private Long reportingTo;
	private Long storeId;
	private String uanNumber;
	private Float workingHours;
	private String payCycle;
	private String employmentType;
	private String jobCode;
	private String secondaryJobCode;
	private java.math.BigDecimal secondaryHourlyRate;
	private java.math.BigDecimal overtimeMultiplier;
	private String esiNumber;
	private String panNumber;

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

	public String getPayCycle() { return payCycle; }
	public void setPayCycle(String payCycle) { this.payCycle = payCycle; }
	public String getEmploymentType() { return employmentType; }
	public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
	public String getJobCode() { return jobCode; }
	public void setJobCode(String jobCode) { this.jobCode = jobCode; }
	public String getSecondaryJobCode() { return secondaryJobCode; }
	public void setSecondaryJobCode(String secondaryJobCode) { this.secondaryJobCode = secondaryJobCode; }
	public java.math.BigDecimal getSecondaryHourlyRate() { return secondaryHourlyRate; }
	public void setSecondaryHourlyRate(java.math.BigDecimal secondaryHourlyRate) { this.secondaryHourlyRate = secondaryHourlyRate; }
	public java.math.BigDecimal getOvertimeMultiplier() { return overtimeMultiplier; }
	public void setOvertimeMultiplier(java.math.BigDecimal overtimeMultiplier) { this.overtimeMultiplier = overtimeMultiplier; }
	public String getEsiNumber() { return esiNumber; }
	public void setEsiNumber(String esiNumber) { this.esiNumber = esiNumber; }
	public String getPanNumber() { return panNumber; }
	public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

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
