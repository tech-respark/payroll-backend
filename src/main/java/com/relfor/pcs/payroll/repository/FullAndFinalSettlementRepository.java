package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.FullAndFinalSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FullAndFinalSettlementRepository extends JpaRepository<FullAndFinalSettlement, Long> {
    List<FullAndFinalSettlement> findByTenantIdAndStoreIdOrderByIdDesc(Long tenantId, Long storeId);
    List<FullAndFinalSettlement> findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(Long tenantId, Long storeId, Long staffId);
}
