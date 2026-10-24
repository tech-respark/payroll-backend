package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PayRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayRunRepository extends JpaRepository<PayRun, Long> {
    Optional<PayRun> findByTenantIdAndStoreIdAndSalaryMonthAndSalaryYearAndCycleType(
            Long tenantId, Long storeId, String salaryMonth, Integer salaryYear, String cycleType);
    List<PayRun> findByTenantIdAndStoreIdOrderBySalaryYearDescIdDesc(Long tenantId, Long storeId);
}
