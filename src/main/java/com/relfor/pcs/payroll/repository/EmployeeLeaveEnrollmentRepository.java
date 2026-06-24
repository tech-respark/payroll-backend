package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.EmployeeLeaveEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeLeaveEnrollmentRepository extends JpaRepository<EmployeeLeaveEnrollment, Long> {
    Optional<EmployeeLeaveEnrollment> findByPersonnelId(Long personnelId);
}
