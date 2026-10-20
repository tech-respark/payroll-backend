package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.ShiftSwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShiftSwapRequestRepository extends JpaRepository<ShiftSwapRequest, Long> {
    List<ShiftSwapRequest> findByTenantIdAndStoreIdOrderByIdDesc(Long tenantId, Long storeId);
    List<ShiftSwapRequest> findByTenantIdAndStoreIdAndRequesterStaffIdOrderByIdDesc(Long tenantId, Long storeId, Long requesterStaffId);

    @Query("select s from ShiftSwapRequest s where s.tenantId = :tenantId and s.storeId = :storeId and (s.requesterStaffId = :staffId or s.targetStaffId = :staffId) order by s.id desc")
    List<ShiftSwapRequest> findInvolvingStaff(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId, @Param("staffId") Long staffId);
}
