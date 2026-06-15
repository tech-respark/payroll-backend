package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.DayWiseAttendanceSummary;
import com.relfor.pcs.payroll.entity.PersonnelAttendance;
import com.relfor.pcs.payroll.projection.MonthlySummaryCalculationProjection;
import com.relfor.pcs.payroll.projection.PersonnelAttendanceSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DayWiseAttendanceSummaryRepository extends JpaRepository<DayWiseAttendanceSummary, Long> {
	@Query(value = "SELECT dwas.personnel_code AS personnelCode, pd.designation AS personnelDesignation,\n" +
			"pd.gender AS personnelGender, pd.personnel_mobile_number AS personnelMobileNumber,\n" +
			"CONCAT(COALESCE(pd.first_name, ''), ' ', COALESCE(pd.last_name, '')) AS personnelName,\n" +
			"dwas.attendance_date AS attendanceDate, dwas.attendance_day_of_week AS attendanceDayOfWeek,\n" +
			"dwas.terminal_serial_number AS terminalSerialNumber,\n" +
			"CASE WHEN :isActualTimeBasedAttendance = false THEN dwas.total_hours_worked_inaday ELSE dwas.sum_of_actual_hours_worked_inaday END AS totalHoursWorkedInADay,\n" +
			"CASE WHEN :isActualTimeBasedAttendance = false THEN 0 ELSE dwas.total_break_time_inaday END AS totalBreakTimeInADay\n" +
			"FROM day_wise_attendance_summary dwas \n" +
			"JOIN personnel_details pd \n" +
			"ON pd.personnel_code = dwas.personnel_code\n" +
			"WHERE dwas.attendance_date BETWEEN :fromDate AND :toDate\n" +
			"AND dwas.tenant_id = :tenantId AND dwas.store_id = :storeId AND dwas.application_name = :applicationName \n" +
			"GROUP BY dwas.personnel_code, dwas.attendance_date, dwas.attendance_day_of_week,\n" +
			"dwas.terminal_serial_number, pd.designation, pd.gender, pd.personnel_mobile_number,\n" +
			"COALESCE(pd.first_name, ''), COALESCE(pd.last_name, '')\n" +
			"ORDER BY dwas.personnel_code, dwas.attendance_date ASC; ", nativeQuery = true)
	List<PersonnelAttendanceSummaryProjection> getAttendanceSummaryBetweenDates(LocalDate fromDate, LocalDate toDate,
																				Long tenantId, Long storeId, String applicationName,
																				boolean isActualTimeBasedAttendance);

	Optional<DayWiseAttendanceSummary> findByTenantIdAndStoreIdAndPersonnelCodeAndApplicationNameAndAttendanceDate(Long tenantId,
																												   Long storeId,
																												   Long personnelCode,
																												   String applicationName,
																												   LocalDate attendanceDate);

	@Query("SELECT dwas FROM DayWiseAttendanceSummary dwas " +
			"WHERE dwas.personnelCode IN :personnelCodes " +
			"AND dwas.attendanceDate IN :attendanceDates " +
			"AND dwas.tenantId = :tenantId " +
			"AND dwas.storeId = :storeId")
	List<DayWiseAttendanceSummary> findExistingDayWiseAttendanceSummaries(
			@Param("personnelCodes") List<Long> personnelCodes,
			@Param("attendanceDates") List<LocalDate> attendanceDates,
			@Param("tenantId") Long tenantId,
			@Param("storeId") Long storeId
	);

	@Query(value = "SELECT \n" +
			"d.tenant_id as tenantId,\n" +
			"d.store_id as storeId,\n" +
			"d.application_name as applicationName,\n" +
			"d.personnel_code as personnelCode,\n" +
			"SUM(CASE WHEN d.is_weekly_off = TRUE THEN 1 ELSE 0 END) AS totalWeeklyOffs,\n" +
			"SUM(CASE WHEN d.working_hours_as_per_roster IS NOT NULL AND d.working_hours_as_per_roster > 0 THEN 1 ELSE 0 END) AS totalWorkingDays,\n" +
			"SUM(CASE WHEN d.is_on_leave = TRUE THEN 1 ELSE 0 END) AS totalPaidLeaves,\n" +
			"SUM(CASE WHEN d.is_absent = TRUE THEN 1 ELSE 0 END) AS totalAbsentDays,\n" +
			"SUM(CASE WHEN d.is_penalty_absent = TRUE THEN 1 ELSE 0 END) AS totalPenaltyAbsentDays,\n" +
			"SUM(CASE WHEN d.is_holiday = TRUE THEN 1 ELSE 0 END) AS totalHolidays,\n" +
			"SUM(CASE WHEN d.is_extra_day = TRUE THEN 1 ELSE 0 END) AS extraDaysWorked,\n" +
			"(SUM(CASE WHEN (d.working_hours_as_per_roster IS NOT NULL AND d.working_hours_as_per_roster > 0) OR d.is_weekly_off = TRUE OR d.is_on_leave = TRUE OR d.is_holiday THEN 1 ELSE 0 END) - " +
			"SUM(CASE WHEN d.is_absent = TRUE OR d.is_penalty_absent THEN 1 ELSE 0 END)) AS totalPaidDays,\n" +
			"SUM(IFNULL(d.early_exit_mins, 0)) AS totalEarlyExitMins,\n" +
			"SUM(IFNULL(d.late_arrival_mins, 0)) AS totalLateArrivalMins,\n" +
			"SUM(CASE WHEN d.diff_between_actual_and_roster_work_mins > 0 THEN d.diff_between_actual_and_roster_work_mins ELSE 0 END) AS totalOvertimeMins,\n" +
			"SUM(CASE WHEN d.is_late_arrival = TRUE THEN 1 ELSE 0 END) AS totalLateArrivals,\n" +
			"SUM(CASE WHEN d.is_early_exit = TRUE THEN 1 ELSE 0 END) AS totalEarlyExits\n" +
			"FROM day_wise_attendance_summary d\n" +
			"WHERE d.tenant_id = :tenantId AND d.store_id = :storeId\n" +
			"AND d.attendance_date BETWEEN :fromDate AND :toDate\n" +
			"AND d.application_name = :applicationName\n" +
			"GROUP BY d.tenant_id, d.store_id, d.application_name, d.personnel_code;", nativeQuery = true)
	List<MonthlySummaryCalculationProjection> calculateMonthlySummary(Long tenantId, Long storeId, String applicationName, LocalDate fromDate, LocalDate toDate);

	Optional<DayWiseAttendanceSummary> findByPersonnelCodeAndAttendanceDate(Long personnelCode, LocalDate attendanceDate);
}