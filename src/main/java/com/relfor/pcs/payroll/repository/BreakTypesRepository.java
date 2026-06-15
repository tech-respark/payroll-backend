package com.relfor.pcs.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.relfor.pcs.payroll.entity.BreakTypes;

@Repository
public interface BreakTypesRepository extends JpaRepository<BreakTypes, Long> {

	@Query(value = "SELECT bt.* FROM break_types bt WHERE bt.store_id = ?1 AND bt.tenant_id = ?2", nativeQuery = true)
	List<BreakTypes> getBreakTypes(Long storeId, Long tenantId);

}