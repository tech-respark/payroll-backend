package com.relfor.pcs.payroll.repository;

import java.util.List;

import com.relfor.pcs.payroll.entity.SShiftsSlots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface SShiftsSlotsRepository extends JpaRepository<SShiftsSlots, Long> {

	List<SShiftsSlots> findByTenantIdAndStoreId(long tenantId, long storeId);

}