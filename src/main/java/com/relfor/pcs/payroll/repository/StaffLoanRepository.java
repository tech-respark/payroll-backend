package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StaffLoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffLoanRepository extends JpaRepository<StaffLoan, Long> {
    List<StaffLoan> findByTenantIdAndStoreIdOrderByIdDesc(Long tenantId, Long storeId);
    List<StaffLoan> findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(Long tenantId, Long storeId, Long staffId);
    List<StaffLoan> findByTenantIdAndStoreIdAndStaffIdAndStatus(Long tenantId, Long storeId, Long staffId, String status);
}
