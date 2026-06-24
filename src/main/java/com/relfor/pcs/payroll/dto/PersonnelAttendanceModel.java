package com.relfor.pcs.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PersonnelAttendanceModel {
	private Long tenantId;
	private Long storeId;
	private LocalDate fromDate;
	private LocalDate toDate;
	private String applicationName;
	@JsonAlias({"personnelCode", "personnelId"})
	private Long personnelId;
	private String personnelDesignation;
	private String personnelName;
	private String personnelGender;
	private String personnelMobileNumber;
	private BigDecimal totalHoursWorkedForPersonnel;
	private BigDecimal totalBreakTimeForPersonnel;
	private String uploadSource;
	private List<DayWiseAttendance> dayWiseAttendanceList;

	public List<DayWiseAttendance> getDayWiseAttendanceList() {
		return dayWiseAttendanceList;
	}

	public void setDayWiseAttendanceList(List<DayWiseAttendance> dayWiseAttendanceList) {
		this.dayWiseAttendanceList = dayWiseAttendanceList;
	}

	public Long getPersonnelId() {
		return personnelId;
	}

	public void setPersonnelId(Long personnelId) {
		this.personnelId = personnelId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public LocalDate getFromDate() {
		return fromDate;
	}

	public void setFromDate(LocalDate fromDate) {
		this.fromDate = fromDate;
	}

	public LocalDate getToDate() {
		return toDate;
	}

	public void setToDate(LocalDate toDate) {
		this.toDate = toDate;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public BigDecimal getTotalHoursWorkedForPersonnel() {
		return totalHoursWorkedForPersonnel;
	}

	public void setTotalHoursWorkedForPersonnel(BigDecimal totalHoursWorkedForPersonnel) {
		this.totalHoursWorkedForPersonnel = totalHoursWorkedForPersonnel;
	}

	public String getPersonnelDesignation() {
		return personnelDesignation;
	}

	public void setPersonnelDesignation(String personnelDesignation) {
		this.personnelDesignation = personnelDesignation;
	}

	public String getPersonnelName() {
		return personnelName;
	}

	public void setPersonnelName(String personnelName) {
		this.personnelName = personnelName;
	}

	public String getPersonnelGender() {
		return personnelGender;
	}

	public void setPersonnelGender(String personnelGender) {
		this.personnelGender = personnelGender;
	}

	public String getPersonnelMobileNumber() {
		return personnelMobileNumber;
	}

	public void setPersonnelMobileNumber(String personnelMobileNumber) {
		this.personnelMobileNumber = personnelMobileNumber;
	}

	public BigDecimal getTotalBreakTimeForPersonnel() {
		return totalBreakTimeForPersonnel;
	}

	public void setTotalBreakTimeForPersonnel(BigDecimal totalBreakTimeForPersonnel) {
		this.totalBreakTimeForPersonnel = totalBreakTimeForPersonnel;
	}

	public String getUploadSource() {
		return uploadSource;
	}

	public void setUploadSource(String uploadSource) {
		this.uploadSource = uploadSource;
	}

	@Override
	public String toString() {
		return "PersonnelAttendanceModel{" +
				"tenantId=" + tenantId +
				", storeId=" + storeId +
				", fromDate=" + fromDate +
				", toDate=" + toDate +
				", applicationName='" + applicationName + '\'' +
				", personnelId=" + personnelId +
				", personnelDesignation='" + personnelDesignation + '\'' +
				", personnelName='" + personnelName + '\'' +
				", personnelGender='" + personnelGender + '\'' +
				", personnelMobileNumber='" + personnelMobileNumber + '\'' +
				", totalHoursWorkedForPersonnel=" + totalHoursWorkedForPersonnel +
				", totalBreakTimeForPersonnel=" + totalBreakTimeForPersonnel +
				", uploadSource='" + uploadSource + '\'' +
				", dayWiseAttendanceList=" + dayWiseAttendanceList +
				'}';
	}
}