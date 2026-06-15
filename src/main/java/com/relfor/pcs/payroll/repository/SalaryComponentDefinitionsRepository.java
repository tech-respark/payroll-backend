package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.SalaryComponentDefinitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SalaryComponentDefinitionsRepository extends JpaRepository<SalaryComponentDefinitions,Long> {
    @Query(value = "SELECT * FROM salary_component_definitions pd \n" +
            "WHERE component_type = :formula and tenant_id = :tenantId and store_id = :storeId and is_computable = true ORDER BY priority_index;", nativeQuery = true)
    List<SalaryComponentDefinitions> findByComponentTypeAndComputableAndTenantIdAndStoreId(String formula, Long tenantId, Long storeId);

    @Query(value = "SELECT * FROM salary_component_definitions pd \n" +
            "WHERE tenant_id = :tenantId and store_id = :storeId ORDER BY priority_index;", nativeQuery = true)
    List<SalaryComponentDefinitions> findByTenantIdAndStoreId(Long tenantId, Long storeId);

    List<SalaryComponentDefinitions> findAllByTenantIdAndStoreId(Long tenantId, Long storeId);
}