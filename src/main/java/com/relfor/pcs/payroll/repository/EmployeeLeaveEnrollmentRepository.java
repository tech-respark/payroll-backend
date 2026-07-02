package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.EmployeeLeaveEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLeaveEnrollmentRepository extends JpaRepository<EmployeeLeaveEnrollment, Long> {
    Optional<EmployeeLeaveEnrollment> findFirstByStaffIdOrderByEnrolledDateDesc(Long staffId);
    List<EmployeeLeaveEnrollment> findByTenantIdAndStoreId(Long tenantId, Long storeId);
}
