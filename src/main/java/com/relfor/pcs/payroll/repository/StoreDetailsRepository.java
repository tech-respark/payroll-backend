package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StoreDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StoreDetailsRepository extends JpaRepository<StoreDetails,Long> {
	@Query(value = "SELECT s.* FROM store_details s " +
			"JOIN tenant_company_mapping t ON s.tenant_company_mapping_id = t.id " +
			"WHERE t.application_name = :applicationName AND t.tenant_id = :tenantId AND s.store_id = :storeId", nativeQuery = true)
	Optional<StoreDetails> fetchStoreAndTenantDetails(String applicationName, Long tenantId, Long storeId);

	@Query(value = "SELECT sd.time_zone FROM store_details sd WHERE sd.store_id = ?1", nativeQuery = true)
	String getStoreTimeZone(Long storeId);

	@Query(value = "SELECT sd.* "
			+ "FROM store_details sd "
			+ "JOIN tenant_company_mapping tcm ON sd.tenant_company_mapping_id = tcm.id "
			+ "WHERE tcm.application_name = :applicationName "
			+ "AND tcm.tenant_id = :tenantId "
			+ "AND (:storeId = 0 OR sd.store_id = :storeId)", nativeQuery = true)
	List<StoreDetails> fetchStoreDetailListForTenant(String applicationName, Long tenantId, Long storeId);
}