package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.SalaryComponentDefinitions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SalaryComponentDefinitionsRepository extends JpaRepository<SalaryComponentDefinitions,Long> {
    @Query(value = "SELECT * FROM salary_component_definitions pd \n" +
            "WHERE component_type = :formula and tenant_id = :tenantId and (store_id = :storeId OR store_id = 0) and is_computable = true ORDER BY priority_index;", nativeQuery = true)
    List<SalaryComponentDefinitions> findByComponentTypeAndComputableAndTenantIdAndStoreId(@org.springframework.data.repository.query.Param("formula") String formula, @org.springframework.data.repository.query.Param("tenantId") Long tenantId, @org.springframework.data.repository.query.Param("storeId") Long storeId);

    @Query(value = "SELECT * FROM salary_component_definitions pd \n" +
            "WHERE tenant_id = :tenantId and (store_id = :storeId OR store_id = 0) ORDER BY priority_index;", nativeQuery = true)
    List<SalaryComponentDefinitions> findByTenantIdAndStoreId(@org.springframework.data.repository.query.Param("tenantId") Long tenantId, @org.springframework.data.repository.query.Param("storeId") Long storeId);

    @Query(value = "SELECT * FROM salary_component_definitions pd \n" +
            "WHERE tenant_id = :tenantId and (store_id = :storeId OR store_id = 0) ORDER BY priority_index;", nativeQuery = true)
    List<SalaryComponentDefinitions> findAllByTenantIdAndStoreId(@org.springframework.data.repository.query.Param("tenantId") Long tenantId, @org.springframework.data.repository.query.Param("storeId") Long storeId);
}