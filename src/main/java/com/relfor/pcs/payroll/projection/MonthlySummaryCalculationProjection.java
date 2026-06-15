package com.relfor.pcs.payroll.projection;

public interface MonthlySummaryCalculationProjection {
	Long getTenantId();
	Long getStoreId();
	String getApplicationName();
	Long getPersonnelCode();
	Float getTotalWeeklyOffs();
	Float getTotalHolidays();
	Float getTotalWorkingDays();
	Float getTotalPaidLeaves();
	Float getTotalAbsentDays();
	Float getTotalPenaltyAbsentDays();
	Float getExtraDaysWorked();
	Float getTotalPaidDays();
	Integer getTotalEarlyExitMins();
	Integer getTotalLateArrivalMins();
	Integer getTotalOvertimeMins();
	Integer getTotalLateArrivals();
	Integer getTotalEarlyExits();
}
