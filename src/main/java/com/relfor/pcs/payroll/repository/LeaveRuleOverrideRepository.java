package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LeaveRuleOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LeaveRuleOverrideRepository extends JpaRepository<LeaveRuleOverride, Long> {
    Optional<LeaveRuleOverride> findByStaffIdAndLeavePlanRuleId(Long staffId, Long leavePlanRuleId);
    List<LeaveRuleOverride> findByTenantIdAndStoreIdAndStaffId(Long tenantId, Long storeId, Long staffId);
}
