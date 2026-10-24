package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.TenantSubscriptionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantSubscriptionEventRepository extends JpaRepository<TenantSubscriptionEvent, Long> {
    List<TenantSubscriptionEvent> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
