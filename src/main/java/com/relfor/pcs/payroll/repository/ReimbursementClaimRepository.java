package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.ReimbursementClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReimbursementClaimRepository extends JpaRepository<ReimbursementClaim, Long> {
    List<ReimbursementClaim> findByTenantIdAndStoreIdOrderByIdDesc(Long tenantId, Long storeId);
    List<ReimbursementClaim> findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(Long tenantId, Long storeId, Long staffId);
    List<ReimbursementClaim> findByTenantIdAndStoreIdAndStaffIdAndSalaryMonthAndSalaryYearAndStatus(
            Long tenantId, Long storeId, Long staffId, String salaryMonth, Integer salaryYear, String status);
}
