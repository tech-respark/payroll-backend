package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    @Query("SELECT COUNT(l) FROM LeaveApplication l " +
           "WHERE l.staffId = :staffId " +
           "AND l.status IN ('PENDING', 'APPROVED') " +
           "AND l.startDate <= :endDate " +
           "AND l.endDate >= :startDate")
    long countOverlappingLeaves(@Param("staffId") Long staffId, 
                                @Param("startDate") LocalDate startDate, 
                                @Param("endDate") LocalDate endDate);

    @Query("SELECT l FROM LeaveApplication l " +
           "WHERE l.staffId = :staffId " +
           "AND l.status = 'APPROVED' " +
           "AND l.startDate <= :endDate " +
           "AND l.endDate >= :startDate")
    List<LeaveApplication> findOverlappingApprovedLeaves(@Param("staffId") Long staffId, 
                                                         @Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);

    List<LeaveApplication> findByStaffIdOrderByCreatedAtDesc(Long staffId);
    
    List<LeaveApplication> findByStatusInOrderByCreatedAtAsc(List<LeaveApplication.ApplicationStatus> statuses);

    List<LeaveApplication> findByTenantIdAndStoreIdAndStatusInOrderByCreatedAtAsc(Long tenantId, Long storeId, List<LeaveApplication.ApplicationStatus> statuses);

    List<LeaveApplication> findByTenantIdAndStoreId(Long tenantId, Long storeId);
}
