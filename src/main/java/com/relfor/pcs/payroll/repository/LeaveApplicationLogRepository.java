package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LeaveApplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveApplicationLogRepository extends JpaRepository<LeaveApplicationLog, Long> {
    List<LeaveApplicationLog> findByLeaveApplicationIdOrderByCreatedAtDesc(Long leaveApplicationId);
}
