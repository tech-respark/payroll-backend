package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LeavePlanRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeavePlanRuleRepository extends JpaRepository<LeavePlanRule, Long> {
    List<LeavePlanRule> findByLeavePlan_Id(Long planId);
}
