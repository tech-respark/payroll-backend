package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LeavePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeavePlanRepository extends JpaRepository<LeavePlan, Long> {
    List<LeavePlan> findByTenantIdAndStoreId(Long tenantId, Long storeId);
}
