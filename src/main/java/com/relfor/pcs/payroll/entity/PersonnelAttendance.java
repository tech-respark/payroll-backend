package com.relfor.pcs.payroll.entity;

//import io.swagger.models.auth.In;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "personnel_attendance")
public class PersonnelAttendance {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long tenantId;
	private Long storeId;
	private String applicationName;
	private String terminalSerialNumber;
	private Long personnelCode;
	private LocalDate attendanceDate;
	private String attendanceDayOfWeek;
	private String punchEvent;
	private Instant punchTimestamp;
	private Long iclockTransactionId;
	private String uploadSource;
	private Instant createdTimestamp;
	private Long createdBy;
	private Instant modifiedTimestamp;
	private Long modifiedBy;
	private Integer sequenceNumberOfPunch;
	private String currentStatus;
	@Version
	private Long versionId;
	private String remark;

	public Long getIclockTransactionId() {
		return iclockTransactionId;
	}

	public void setIclockTransactionId(Long iclockTransactionId) {
		this.iclockTransactionId = iclockTransactionId;
	}

	public Instant getPunchTimestamp() {
		return punchTimestamp;
	}

	public void setPunchTimestamp(Instant punchTimestamp) {
		this.punchTimestamp = punchTimestamp;
	}

	public String getPunchEvent() {
		return punchEvent;
	}

	public void setPunchEvent(String punchEvent) {
		this.punchEvent = punchEvent;
	}

	public LocalDate getAttendanceDate() {
		return attendanceDate;
	}

	public void setAttendanceDate(LocalDate attendanceDate) {
		this.attendanceDate = attendanceDate;
	}

	public Long getPersonnelCode() {
		return personnelCode;
	}

	public void setPersonnelCode(Long personnelCode) {
		this.personnelCode = personnelCode;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTerminalSerialNumber() {
		return terminalSerialNumber;
	}

	public void setTerminalSerialNumber(String terminalSerialNumber) {
		this.terminalSerialNumber = terminalSerialNumber;
	}

	public String getAttendanceDayOfWeek() {
		return attendanceDayOfWeek;
	}

	public void setAttendanceDayOfWeek(String attendanceDayOfWeek) {
		this.attendanceDayOfWeek = attendanceDayOfWeek;
	}

	public String getUploadSource() {
		return uploadSource;
	}

	public void setUploadSource(String uploadSource) {
		this.uploadSource = uploadSource;
	}

	public Integer getSequenceNumberOfPunch() {
		return sequenceNumberOfPunch;
	}

	public void setSequenceNumberOfPunch(Integer sequenceNumberOfPunch) {
		this.sequenceNumberOfPunch = sequenceNumberOfPunch;
	}

	public Instant getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Instant createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getModifiedTimestamp() {
		return modifiedTimestamp;
	}

	public void setModifiedTimestamp(Instant modifiedTimestamp) {
		this.modifiedTimestamp = modifiedTimestamp;
	}

	public Long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getVersionId() {
		return versionId;
	}

	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}

	@Override
	public String toString() {
		return "PersonnelAttendance{" +
				"id=" + id +
				", tenantId=" + tenantId +
				", storeId=" + storeId +
				", applicationName='" + applicationName + '\'' +
				", terminalSerialNumber='" + terminalSerialNumber + '\'' +
				", personnelCode=" + personnelCode +
				", attendanceDate=" + attendanceDate +
				", attendanceDayOfWeek='" + attendanceDayOfWeek + '\'' +
				", punchEvent='" + punchEvent + '\'' +
				", punchTimestamp=" + punchTimestamp +
				", iclockTransactionId=" + iclockTransactionId +
				", uploadSource='" + uploadSource + '\'' +
				", createdTimestamp=" + createdTimestamp +
				", createdBy=" + createdBy +
				", modifiedTimestamp=" + modifiedTimestamp +
				", modifiedBy=" + modifiedBy +
				", sequenceNumberOfPunch=" + sequenceNumberOfPunch +
				", currentStatus='" + currentStatus + '\'' +
				", versionId=" + versionId +
				", remark='" + remark + '\'' +
				'}';
	}

	public String toLogString() {
		return "PersonnelAttendance{" +
				"id=" + id +
				", tenantId=" + tenantId +
				", storeId=" + storeId +
				", applicationName='" + applicationName + '\'' +
				", personnelCode=" + personnelCode +
				", attendanceDate=" + attendanceDate +
				", punchTimestamp=" + punchTimestamp +
				", uploadSource='" + uploadSource + '\'' +
				", currentStatus='" + currentStatus + '\'' +
				'}';
	}
}