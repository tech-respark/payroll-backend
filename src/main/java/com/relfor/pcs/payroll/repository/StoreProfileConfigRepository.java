package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StoreProfileConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreProfileConfigRepository extends JpaRepository<StoreProfileConfig, Long> {
    Optional<StoreProfileConfig> findByTenantIdAndStoreId(Long tenantId, Long storeId);
}
