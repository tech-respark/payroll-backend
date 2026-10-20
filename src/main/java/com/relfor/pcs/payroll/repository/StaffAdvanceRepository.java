package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StaffAdvance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffAdvanceRepository extends JpaRepository<StaffAdvance, Long> {
    List<StaffAdvance> findByTenantIdAndStoreIdOrderByIdDesc(Long tenantId, Long storeId);
    List<StaffAdvance> findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(Long tenantId, Long storeId, Long staffId);
    List<StaffAdvance> findByTenantIdAndStoreIdAndStaffIdAndStatus(Long tenantId, Long storeId, Long staffId, String status);
}
