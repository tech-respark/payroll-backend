package com.relfor.pcs.payroll.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PersonnelAttendanceSummaryProjection {
	Long getStaffId();
	String getPersonnelName();
	String getPersonnelDesignation();
	String getPersonnelGender();
	String getPersonnelMobileNumber();
	LocalDate getAttendanceDate();
	String getAttendanceDayOfWeek();
	String getTerminalSerialNumber();
	BigDecimal getTotalHoursWorkedInADay();
	BigDecimal getTotalBreakTimeInADay();
}
