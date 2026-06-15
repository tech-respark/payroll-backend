package com.relfor.pcs.payroll.projection;

public interface TenantStoreProjection {
	Long getTenantId();
	Long getStoreId();
	String getApplicationName();
	String getPersonnelCompanyId();
	String getTimeZone();
	Boolean getIsActualTimeBasedAttendance();
	String getPenaltyAbsentDays();
	Boolean getIsPaidLeaveApplicable();
}
