package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonAlias;

public class IndividualPunches {
//	private Long personnelAttendanceRequestId;
	private Long personnelAttendanceId;
	private Long serialNumber;
	private String punchEvent;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate punchDate;
	@JsonFormat(pattern = "HH:mm")
	private LocalTime punchTime;
	private String currentStatus;
	private Instant createdTimestamp;
	private Long createdBy;
	private Instant modifiedTimestamp;
	private Long modifiedBy;
	private Integer punchDateOffset;
	private Long staffId;
	private LocalDate attendanceDate;
	private String uploadSource;
	private String remark;

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public LocalTime getPunchTime() {
		return punchTime;
	}

	public void setPunchTime(LocalTime punchTime) {
		this.punchTime = punchTime;
	}

	public Long getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Long serialNumber) {
		this.serialNumber = serialNumber;
	}

	public LocalDate getPunchDate() {
		return punchDate;
	}

	public void setPunchDate(LocalDate punchDate) {
		this.punchDate = punchDate;
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

//	public Long getPersonnelAttendanceRequestId() {
//		return personnelAttendanceRequestId;
//	}
//
//	public void setPersonnelAttendanceRequestId(Long personnelAttendanceRequestId) {
//		this.personnelAttendanceRequestId = personnelAttendanceRequestId;
//	}

	public Integer getPunchDateOffset() {
		return punchDateOffset;
	}

	public void setPunchDateOffset(Integer punchDateOffset) {
		this.punchDateOffset = punchDateOffset;
	}

	public String getPunchEvent() {
		return punchEvent;
	}

	public void setPunchEvent(String punchEvent) {
		this.punchEvent = punchEvent;
	}

	public String getUploadSource() {
		return uploadSource;
	}

	public void setUploadSource(String uploadSource) {
		this.uploadSource = uploadSource;
	}

	public LocalDate getAttendanceDate() {
		return attendanceDate;
	}

	public void setAttendanceDate(LocalDate attendanceDate) {
		this.attendanceDate = attendanceDate;
	}

	public Long getStaffId() {
		return staffId;
	}

	public void setStaffId(Long staffId) {
		this.staffId = staffId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getPersonnelAttendanceId() {
		return personnelAttendanceId;
	}

	public void setPersonnelAttendanceId(Long personnelAttendanceId) {
		this.personnelAttendanceId = personnelAttendanceId;
	}

	@Override
	public String toString() {
		return "IndividualPunches{" +
				"personnelAttendanceId=" + personnelAttendanceId +
				", serialNumber=" + serialNumber +
				", punchEvent='" + punchEvent + '\'' +
				", punchDate=" + punchDate +
				", punchTime=" + punchTime +
				", currentStatus='" + currentStatus + '\'' +
				", createdTimestamp=" + createdTimestamp +
				", createdBy=" + createdBy +
				", modifiedTimestamp=" + modifiedTimestamp +
				", modifiedBy=" + modifiedBy +
				", punchDateOffset=" + punchDateOffset +
				", staffId=" + staffId +
				", attendanceDate=" + attendanceDate +
				", uploadSource='" + uploadSource + '\'' +
				", remark='" + remark + '\'' +
				'}';
	}
}
