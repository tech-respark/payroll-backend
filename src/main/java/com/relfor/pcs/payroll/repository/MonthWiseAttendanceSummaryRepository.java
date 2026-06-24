package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.MonthWiseAttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MonthWiseAttendanceSummaryRepository extends JpaRepository<MonthWiseAttendanceSummary, Long> {

    @Query(value = "SELECT * FROM month_wise_attendance_summary pd \n" +
            "WHERE staff_id = :staffId and salary_month = :month and salary_year = :year ;", nativeQuery = true)
    MonthWiseAttendanceSummary getSummaryByStaffIdAndMonth(Long staffId, String month, Long year);

    @Query(value = "SELECT * "
            + "FROM month_wise_attendance_summary mwas "
            + "WHERE mwas.tenant_id = :tenantId "
            + "AND (:storeId = 0 OR mwas.store_id = :storeId) "
            + "AND mwas.application_name = :applicationName "
            + "AND mwas.salary_month LIKE :salaryMonth "
            + "AND mwas.salary_year = :salaryYear", nativeQuery = true)
    List<MonthWiseAttendanceSummary> getExistingMonthWiseSummaryList(String applicationName, Long tenantId, Long storeId, String salaryMonth, Integer salaryYear);
}