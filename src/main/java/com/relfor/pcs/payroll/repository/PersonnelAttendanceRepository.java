package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PersonnelAttendance;
import com.relfor.pcs.payroll.projection.PersonnelAttendanceProjectionForInOutHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PersonnelAttendanceRepository extends JpaRepository<PersonnelAttendance,Long> {
	@Query(value = "SELECT pa.tenant_id AS tenantId, pa.store_id AS storeId,\n" +
			"pa.personnel_code AS personnelCode, pd.designation AS personnelDesignation,\n" +
			"pd.gender AS personnelGender, pd.personnel_mobile_number AS personnelMobileNumber,\n" +
			"CONCAT(COALESCE(pd.first_name, ''), ' ', COALESCE(pd.last_name, '')) AS personnelName,\n" +
			"pa.attendance_date AS attendanceDate, pa.attendance_day_of_week AS attendanceDayOfWeek,\n" +
			"pa.terminal_serial_number AS terminalSerialNumber, pa.punch_event AS punchEvent,\n" +
			"pa.punch_timestamp AS punchTimestamp, pa.iclock_transaction_id AS iclockTransactionId,\n" +
			"pa.created_timestamp AS createdTimestamp, pa.created_by AS createdBy, pa.upload_source AS uploadSource,\n" +
			"pa.modified_timestamp AS modifiedTimestamp, pa.modified_by AS modifiedBy,\n" +
			"pa.sequence_number_of_punch AS sequenceNumberOfPunch, pa.application_name AS applicationName,\n" +
			"pa.id AS personnelAttendanceId, \n" +
			"pa.current_status AS currentStatus \n" +
			"FROM personnel_attendance pa\n" +
			"JOIN personnel_details pd \n" +
			"ON pd.personnel_code = pa.personnel_code \n" +
			"WHERE pa.attendance_date BETWEEN :fromDate AND :toDate \n" +
			"AND pa.tenant_id = :tenantId AND pa.store_id = :storeId \n" +
			"AND pa.application_name = :applicationName AND pa.personnel_code IN :personnelCodes ;", nativeQuery = true)
	List<PersonnelAttendanceProjectionForInOutHistory> getInOutHistoryBetweenDates(LocalDate fromDate, LocalDate toDate, String applicationName,
																				   Long tenantId, Long storeId, List<Long> personnelCodes);

	List<PersonnelAttendance> findByIdIn(List<Long> personnelAttendanceIdList);

	@Query(value = "SELECT * "
			+ "FROM personnel_attendance pa "
			+ "WHERE pa.tenant_id = :tenantId "
			+ "AND (:storeId = 0 OR pa.store_id = :storeId) "
			+ "AND pa.application_name = :applicationName "
			+ "AND pa.upload_source = :uploadSource "
			+ "AND punch_timestamp BETWEEN :fromDateTime AND :toDateTime", nativeQuery = true)
	List<PersonnelAttendance> findExistingPersonnelAttendanceData(Long tenantId, Long storeId,
													  String applicationName, String uploadSource,
													  LocalDateTime fromDateTime, LocalDateTime toDateTime);
}