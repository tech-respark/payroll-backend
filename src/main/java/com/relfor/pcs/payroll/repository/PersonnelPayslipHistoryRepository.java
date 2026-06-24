package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PersonnelPayslipHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonnelPayslipHistoryRepository extends JpaRepository<PersonnelPayslipHistory, Long> {

    Optional<PersonnelPayslipHistory> findByStaffIdAndSalaryMonthAndSalaryYear(Long staffId, String salaryMonth, Integer salaryYear);
    List<PersonnelPayslipHistory> findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYear(Long tenantId, Long storeId, String salaryMonth, Integer salaryYear);
}