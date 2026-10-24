package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.TenantSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, Long> {
    Optional<TenantSubscription> findByTenantId(Long tenantId);
}
