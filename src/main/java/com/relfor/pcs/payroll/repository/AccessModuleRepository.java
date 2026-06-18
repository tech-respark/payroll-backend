package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.AccessModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessModuleRepository extends JpaRepository<AccessModule, Long> {
    List<AccessModule> findByTenantIdAndStoreId(Long tenantId, Long storeId);
}
