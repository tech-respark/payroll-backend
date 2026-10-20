package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StaffAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffAvailabilityRepository extends JpaRepository<StaffAvailability, Long> {
    List<StaffAvailability> findByTenantIdAndStoreIdAndStaffId(Long tenantId, Long storeId, Long staffId);
    Optional<StaffAvailability> findByTenantIdAndStoreIdAndStaffIdAndWeekday(Long tenantId, Long storeId, Long staffId, String weekday);
}
