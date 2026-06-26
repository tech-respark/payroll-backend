package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    Optional<LeaveType> findByLeaveCodeAndTenantIdAndStoreId(String leaveCode, Long tenantId, Long storeId);
    List<LeaveType> findByTenantIdAndStoreId(Long tenantId, Long storeId);
}
