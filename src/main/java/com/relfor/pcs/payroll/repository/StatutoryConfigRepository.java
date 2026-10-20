package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StatutoryConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatutoryConfigRepository extends JpaRepository<StatutoryConfig, Long> {
    Optional<StatutoryConfig> findByTenantIdAndStoreId(Long tenantId, Long storeId);
}
