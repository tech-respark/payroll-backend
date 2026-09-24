package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.AttendanceRegularizationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface AttendanceRegularizationRequestRepository extends JpaRepository<AttendanceRegularizationRequest, Long> {

    @Query("SELECT r FROM AttendanceRegularizationRequest r JOIN PersonnelDetails pd ON r.staffId = pd.id " +
           "WHERE pd.applicationTenantId = :tenantId AND pd.storeId = :storeId " +
           "AND r.status = :status " +
           "ORDER BY r.createdAt ASC")
    List<AttendanceRegularizationRequest> findByTenantAndStoreAndStatus(
            @Param("tenantId") Long tenantId, 
            @Param("storeId") Long storeId, 
            @Param("status") AttendanceRegularizationRequest.RegularizationStatus status);

    @Query("SELECT r FROM AttendanceRegularizationRequest r JOIN PersonnelDetails pd ON r.staffId = pd.id " +
           "WHERE pd.applicationTenantId = :tenantId AND pd.storeId = :storeId " +
           "AND pd.reportingTo = :managerId " +
           "AND r.status = :status " +
           "ORDER BY r.createdAt ASC")
    List<AttendanceRegularizationRequest> findByTenantAndStoreAndReportingToAndStatus(
            @Param("tenantId") Long tenantId, 
            @Param("storeId") Long storeId, 
            @Param("managerId") Long managerId, 
            @Param("status") AttendanceRegularizationRequest.RegularizationStatus status);
}
