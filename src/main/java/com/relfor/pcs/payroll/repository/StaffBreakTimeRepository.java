package com.relfor.pcs.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.relfor.pcs.payroll.entity.StaffBreakTime;

@Repository
public interface StaffBreakTimeRepository extends JpaRepository<StaffBreakTime, Long> {

	@Query(value = "SELECT sbt.*  FROM staff_break_time sbt join staff_shifts ss on ss.id = sbt.staff_shift_id WHERE ss.store_id = ?1 AND ss.tenant_id = ?2 and ss.active = ?3", nativeQuery = true)
	List<StaffBreakTime> getStaffBreakTime(Long storeId, Long tenantId, Boolean active);

	@Query(value = "SELECT sbt.* FROM staff_break_time sbt WHERE sbt.staff_id = ?1 And sbt.staff_shift_id = ?2", nativeQuery = true)
	List<StaffBreakTime> getStaffBreakTimeData(Long staffId, Long staffShiftId);

	@Query(value = "SELECT sbt.*  FROM staff_break_time sbt join staff_shifts ss on ss.id = sbt.staff_shift_id WHERE ss.tenant_id = ?1  AND ss.store_id = ?2 And ss.shift_date = ?3", nativeQuery = true)
	List<StaffBreakTime> getStaffBreakTimeByTenantIdAndStoreIdAndDate(Long tenantId, Long storeId, String date);

	@Query(value = "SELECT sbt.* FROM staff_break_time sbt WHERE sbt.staff_id = ?1", nativeQuery = true)
	List<StaffBreakTime> getStaffBreakTimeByStaffId(Long staffId);

	List<StaffBreakTime> findByStaffShiftId(long id);

	@Modifying
	@Transactional
	@Query("DELETE FROM StaffBreakTime sbt WHERE sbt.staffShiftId IS NULL")
	void deleteWhereStaffShiftIdIsNull();

	@Query(value = "SELECT sbt.*  FROM staff_break_time sbt join staff_shifts ss on ss.id = sbt.staff_shift_id WHERE sbt.staff_id = ?1  And ss.shift_date = ?2", nativeQuery = true)
	List<StaffBreakTime> getStaffBreakTimeByStaffIdAndDate(Long staffId, String date);

}