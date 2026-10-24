package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.SalaryRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalaryRevisionRepository extends JpaRepository<SalaryRevision, Long> {
    List<SalaryRevision> findByTenantIdAndStoreIdOrderByIdDesc(Long tenantId, Long storeId);
    List<SalaryRevision> findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(Long tenantId, Long storeId, Long staffId);
}
