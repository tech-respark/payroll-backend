package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.TenantCompanyMapping;
import com.relfor.pcs.payroll.projection.TenantStoreProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TenantCompanyMappingRepository extends JpaRepository<TenantCompanyMapping,Long> {
	@Query(value = "SELECT tcm.tenant_id tenantId, sd.store_id AS storeId, tcm.application_name AS applicationName,\n" +
			"tcm.personnel_company_id AS personnelCompanyId, sd.time_zone AS timeZone,\n" +
			"sd.is_actual_time_based_attendance AS isActualTimeBasedAttendance,\n" +
			"tcm.penalty_absent_days AS penaltyAbsentDays,\n" +
			"tcm.is_paid_leave_applicable AS isPaidLeaveApplicable\n" +
			"FROM tenant_company_mapping tcm \n" +
			"JOIN store_details sd ON tcm.id = sd.tenant_company_mapping_id \n" +
			"WHERE tcm.tenant_id = :tenantId \n" +
			"AND (:storeId = 0 OR sd.store_id = :storeId) " +
			"AND tcm.application_name = :applicationName LIMIT 1;", nativeQuery = true)
	Optional<TenantStoreProjection> getTenantStoreMapping(Long tenantId, Long storeId, String applicationName);

	Optional<TenantCompanyMapping> findByApplicationNameAndTenantId(String applicationName, Long tenantId);
}