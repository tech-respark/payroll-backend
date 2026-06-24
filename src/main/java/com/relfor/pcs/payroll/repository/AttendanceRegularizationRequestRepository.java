package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.AttendanceRegularizationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRegularizationRequestRepository extends JpaRepository<AttendanceRegularizationRequest, Long> {
}
