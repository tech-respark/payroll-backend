package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PersonnelDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PersonnelDetailsRepository extends JpaRepository<PersonnelDetails,Long> {
	Optional<PersonnelDetails> findByIdAndApplicationName(Long id, String applicationName);
	Optional<PersonnelDetails> findByUsername(String username);
	Optional<PersonnelDetails> findByEmail(String email);
	List<PersonnelDetails> findByApplicationTenantIdAndStoreId(Long applicationTenantId, Long storeId);

	@Query(value = "SELECT * FROM personnel_details pd \n" +
			"WHERE id IN (:idList) ;", nativeQuery = true)
	List<PersonnelDetails> getPersonnelById(List<Long> idList);

    Optional<PersonnelDetails> findById(Long id);

	@Query(value = "SELECT * FROM personnel_details pd \n" +
			"WHERE pd.application_tenant_id = :tenantId AND pd.employee_code IN (:employeeCodeList) ;", nativeQuery = true)
	List<PersonnelDetails> getPersonnelByEmployeeCode(Long tenantId, List<String> employeeCodeList);

	@Query(value = "SELECT pd.working_hours FROM personnel_details pd \n" +
			"WHERE pd.id = :staffId", nativeQuery = true)
	Float getPersonnelWorkingHours(Long staffId);

	long countByApplicationTenantIdAndStoreId(Long applicationTenantId, Long storeId);
}