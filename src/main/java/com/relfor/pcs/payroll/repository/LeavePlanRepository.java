package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LeavePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeavePlanRepository extends JpaRepository<LeavePlan, Long> {
}
