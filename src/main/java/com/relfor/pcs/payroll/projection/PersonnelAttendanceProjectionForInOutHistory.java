package com.relfor.pcs.payroll.projection;

import java.time.Instant;
import java.time.LocalDate;

public interface PersonnelAttendanceProjectionForInOutHistory {
	Long getTenantId();
	Long getStoreId();
	String getApplicationName();
	Long getPersonnelCode();
	String getPersonnelName();
	String getPersonnelDesignation();
	String getPersonnelGender();
	String getPersonnelMobileNumber();
	LocalDate getAttendanceDate();
	String getAttendanceDayOfWeek();
	String getTerminalSerialNumber();
	String getPunchEvent();
	Instant getPunchTimestamp();
	Long getIclockTransactionId();
	Instant getCreatedTimestamp();
	Long getCreatedBy();
	Instant getModifiedTimestamp();
	Long getModifiedBy();
	String getUploadSource();
	Long getSequenceNumberOfPunch();
	String getCurrentStatus();
	Long getPersonnelAttendanceId();
}
