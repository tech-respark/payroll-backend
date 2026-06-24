package com.relfor.pcs.payroll.projection;

import java.time.Instant;
import java.time.LocalDate;

public interface PersonnelAttendanceRequestsProjection {
	Long getPersonnelAttendanceRequestId();
	Long getTenantId();
	Long getStoreId();
	String getApplicationName();
	Long getStaffId();
	String getPersonnelName();
	String getPersonnelDesignation();
	String getPersonnelGender();
	String getPersonnelMobileNumber();
	LocalDate getAttendanceDate();
	String getAttendanceDayOfWeek();
	String getPunchEvent();
	Instant getPunchTimestamp();
	Instant getCreatedTimestamp();
	Long getCreatedBy();
	Instant getModifiedTimestamp();
	Long getModifiedBy();
	String getRemark();
	String getCurrentStatus();
}
