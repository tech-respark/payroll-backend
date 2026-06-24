package com.relfor.pcs.payroll.model;

import com.relfor.pcs.payroll.dto.IndividualPunches;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

public class AttendanceRequestsDTO {
//	private Long personnelAttendanceRequestId;
	private Long personnelAttendanceId;
	private Long tenantId;
	private Long storeId;
	private String applicationName;
	@JsonAlias({"personnelCode", "personnelId"})
	private Long personnelId;
	private String personnelName;
	private String personnelDesignation;
	private String personnelGender;
	private String personnelMobileNumber;
	private LocalDate attendanceDate;
	private String attendanceDayOfWeek;
	private LocalDate punchDate;
	private LocalTime punchTime;
	private String punchEvent;
	private Instant createdTimestamp;
	private Long createdBy;
	private Instant modifiedTimestamp;
	private Long modifiedBy;
	private String currentStatus;
	private String remark;
	private List<IndividualPunches> individualPunchesList;

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public Long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Instant getModifiedTimestamp() {
		return modifiedTimestamp;
	}

	public void setModifiedTimestamp(Instant modifiedTimestamp) {
		this.modifiedTimestamp = modifiedTimestamp;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Instant createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public LocalTime getPunchTime() {
		return punchTime;
	}

	public void setPunchTime(LocalTime punchTime) {
		this.punchTime = punchTime;
	}

	public LocalDate getPunchDate() {
		return punchDate;
	}

	public void setPunchDate(LocalDate punchDate) {
		this.punchDate = punchDate;
	}

	public String getAttendanceDayOfWeek() {
		return attendanceDayOfWeek;
	}

	public void setAttendanceDayOfWeek(String attendanceDayOfWeek) {
		this.attendanceDayOfWeek = attendanceDayOfWeek;
	}

	public LocalDate getAttendanceDate() {
		return attendanceDate;
	}

	public void setAttendanceDate(LocalDate attendanceDate) {
		this.attendanceDate = attendanceDate;
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

//	public Long getPersonnelAttendanceRequestId() {
//		return personnelAttendanceRequestId;
//	}
//
//	public void setPersonnelAttendanceRequestId(Long personnelAttendanceRequestId) {
//		this.personnelAttendanceRequestId = personnelAttendanceRequestId;
//	}

	public String getPersonnelMobileNumber() {
		return personnelMobileNumber;
	}

	public void setPersonnelMobileNumber(String personnelMobileNumber) {
		this.personnelMobileNumber = personnelMobileNumber;
	}

	public String getPersonnelGender() {
		return personnelGender;
	}

	public void setPersonnelGender(String personnelGender) {
		this.personnelGender = personnelGender;
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

	public String getPunchEvent() {
		return punchEvent;
	}

	public void setPunchEvent(String punchEvent) {
		this.punchEvent = punchEvent;
	}

	public List<IndividualPunches> getIndividualPunchesList() {
		return individualPunchesList;
	}

	public void setIndividualPunchesList(List<IndividualPunches> individualPunchesList) {
		this.individualPunchesList = individualPunchesList;
	}

	public Long getPersonnelAttendanceId() {
		return personnelAttendanceId;
	}

	public void setPersonnelAttendanceId(Long personnelAttendanceId) {
		this.personnelAttendanceId = personnelAttendanceId;
	}
}
