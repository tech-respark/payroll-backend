package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.CommissionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommissionPlanRepository extends JpaRepository<CommissionPlan, Long> {
    @Query("SELECT DISTINCT p FROM CommissionPlan p LEFT JOIN FETCH p.slabs WHERE p.tenantId = :tenantId AND p.storeId = :storeId")
    List<CommissionPlan> findByTenantIdAndStoreId(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);

    @Query("SELECT DISTINCT p FROM CommissionPlan p LEFT JOIN FETCH p.slabs WHERE p.tenantId = :tenantId AND p.storeId = :storeId AND p.active = true")
    List<CommissionPlan> findByTenantIdAndStoreIdAndActiveTrue(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);

    @Query("SELECT DISTINCT p FROM CommissionPlan p LEFT JOIN FETCH p.slabs WHERE p.id = :id")
    Optional<CommissionPlan> findWithSlabsById(@Param("id") Long id);
}
