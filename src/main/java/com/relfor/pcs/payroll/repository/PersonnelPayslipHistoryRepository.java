package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PersonnelPayslipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonnelPayslipHistoryRepository extends JpaRepository<PersonnelPayslipHistory, Long> {

    Optional<PersonnelPayslipHistory> findByStaffIdAndSalaryMonthAndSalaryYear(Long staffId, String salaryMonth, Integer salaryYear);
    List<PersonnelPayslipHistory> findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYear(Long tenantId, Long storeId, String salaryMonth, Integer salaryYear);

    @Query(value = "SELECT salary_month, salary_year FROM personnel_payslip_history "
            + "WHERE tenant_id = :tenantId AND store_id = :storeId "
            + "ORDER BY salary_year DESC, id DESC LIMIT 1", nativeQuery = true)
    List<Object[]> findLatestMonth(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);
}