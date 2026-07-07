package com.relfor.pcs.payroll.repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.relfor.pcs.payroll.entity.SStaffShifts;

@Repository
@Transactional
public interface SStaffShiftsRepository extends JpaRepository<SStaffShifts, Long> {

	List<SStaffShifts> findByTenantIdAndStoreId(long tenantId, long storeId);

	@Query(value = "SELECT ss.*, s.designation FROM staff_shifts ss join personnel_details s on ss.staff_id = s.staff_id WHERE ss.tenant_id = ?1 and ss.store_id = ?2 and ss.shift_date = ?3", nativeQuery = true)
	List<SStaffShifts> findByTenantIdAndStoreIdAndShiftDate(long tenantId, long storeId, LocalDate day);

	List<SStaffShifts> findByTenantIdAndStoreIdAndShiftDateAndStaffId(long tenantId, long storeId, LocalDate day,
			long staffId);

	@Query(value = "SELECT * " + "FROM staff_shifts " + "WHERE staff_shifts.tenant_id = :tenantId "
			+ "AND staff_shifts.store_id = :storeId " + "AND staff_shifts.staff_id = :staffId "
			+ "AND staff_shifts.shift_date >= :startDate "
			+ "AND staff_shifts.shift_date < :endDate", nativeQuery = true)
	List<SStaffShifts> findByTenantIdAndStoreIdAndStaffIdAndShiftDate(long tenantId, long storeId, long staffId,
			String startDate, String endDate);

	@Query(value = "SELECT * FROM staff_shifts WHERE tenant_id = :tenantId "
			+ "AND store_id = :storeId AND staff_id IN (:staffIds) "
			+ "AND shift_date >= :startDate AND shift_date < :endDate", nativeQuery = true)
	List<SStaffShifts> findByTenantIdAndStoreIdAndStaffIdInAndShiftDate(
			@Param("tenantId") long tenantId, @Param("storeId") long storeId, 
			@Param("staffIds") List<Long> staffIds,
			@Param("startDate") String startDate, @Param("endDate") String endDate);

	@Query(value = "SELECT ss.* FROM staff_shifts ss WHERE ss.id = ?1", nativeQuery = true)
	SStaffShifts getStaffShift(Long staffShiftId);

	@Query(value = "SELECT * " + "FROM staff_shifts ss " + "WHERE ss.tenant_id = :tenantId "
			+ "AND ss.store_id = :storeId " + "AND ss.staff_id = :staffId "
			+ "AND ss.shift_date >= :shiftDate ", nativeQuery = true)
	List<SStaffShifts> findByTenantIdAndStoreIdAndStaffIdAndShiftDate(long tenantId, long storeId, long staffId,
			LocalDate shiftDate);

	@Query(value = "SELECT * FROM staff_shifts ss WHERE ss.tenant_id = ?1 AND ss.store_id = ?2 AND ss.staff_id In ?3 AND ss.shift_date =?4 ", nativeQuery = true)
	List<SStaffShifts> getStaffShifts(Long tenantId, Long storeId, List<Long> staffIdsList, String shiftDate);

	@Query(value = "SELECT * FROM staff_shifts ss WHERE  ss.shift_date  between ?1 and ?2  ", nativeQuery = true)
	List<SStaffShifts> getExistingStaffShiftsBetween(String minDate, String maxDate);

	@Query(value = "SELECT ss.slot FROM staff_shifts ss WHERE ss.staff_id = ?1 and ss.shift_date = ?2 and ss.tenant_id = ?3 and ss.store_id = ?4 and ss.on_leave = 0 && ss.weekly_off = 0", nativeQuery = true)
	String getStaffShifts(long staffId, Date date, long tenantId, long storeId);

	@Query(value = " SELECT ss.staff_id, COUNT(ss.id) as shiftCount FROM staff_shifts ss WHERE ss.shift_date BETWEEN :fromDate AND :toDate AND ss.tenant_id = :tenantId  AND ss.store_id = :storeId and ss.on_leave = 0 GROUP BY ss.staff_id", nativeQuery = true)
	List<Object[]> findStaffShiftCounts(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate,
			@Param("tenantId") long tenantId, @Param("storeId") long storeId);

	@Query(value = "SELECT staff_id FROM staff_shifts WHERE tenant_id = :tenantId AND store_id = :storeId AND shift_date = :shiftDate", nativeQuery = true)
	List<Long> findStaffIdsByTenantStoreAndDate(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId, @Param("shiftDate") String shiftDate);

	List<SStaffShifts> findByTenantIdAndStoreIdAndStaffIdAndShiftDateBetween(long tenantId, long storeId, long staffId, LocalDate startDate, LocalDate endDate);
}