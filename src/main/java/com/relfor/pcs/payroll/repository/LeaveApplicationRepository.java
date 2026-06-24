package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    @Query("SELECT COUNT(l) FROM LeaveApplication l " +
           "WHERE l.personnelId = :personnelId " +
           "AND l.status IN ('PENDING', 'APPROVED') " +
           "AND l.startDate <= :endDate " +
           "AND l.endDate >= :startDate")
    long countOverlappingLeaves(@Param("personnelId") Long personnelId, 
                                @Param("startDate") LocalDate startDate, 
                                @Param("endDate") LocalDate endDate);
}
