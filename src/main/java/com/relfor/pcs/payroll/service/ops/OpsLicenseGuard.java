package com.relfor.pcs.payroll.service.ops;

import com.relfor.pcs.payroll.entity.TenantSubscription;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.StoreDetailsRepository;
import com.relfor.pcs.payroll.repository.TenantSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class OpsLicenseGuard {

    private final TenantSubscriptionRepository subscriptionRepository;
    private final StoreDetailsRepository storeDetailsRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;

    public OpsLicenseGuard(
            TenantSubscriptionRepository subscriptionRepository,
            StoreDetailsRepository storeDetailsRepository,
            PersonnelDetailsRepository personnelDetailsRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.storeDetailsRepository = storeDetailsRepository;
        this.personnelDetailsRepository = personnelDetailsRepository;
    }

    public TenantSubscription require(Long tenantId) {
        return subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> OpsException.notFound("No subscription for tenant " + tenantId));
    }

    public TenantSubscription.Status effectiveStatus(TenantSubscription subscription) {
        if (subscription.getStatus() == TenantSubscription.Status.ACTIVE
                || subscription.getStatus() == TenantSubscription.Status.TRIAL) {
            if (subscription.getEndsOn() != null && LocalDate.now().isAfter(subscription.getEndsOn())) {
                return TenantSubscription.Status.EXPIRED;
            }
        }
        return subscription.getStatus();
    }

    public void assertCanAddStore(Long tenantId) {
        TenantSubscription subscription = require(tenantId);
        if (subscription.getMaxStores() == null) {
            return;
        }
        long current = storeDetailsRepository.fetchStoreDetailListForTenant(tenantId, 0L).size();
        if (current >= subscription.getMaxStores()) {
            throw OpsException.conflict("Store limit reached for this license.");
        }
    }

    public void assertCanAddStaff(Long tenantId) {
        TenantSubscription subscription = subscriptionRepository.findByTenantId(tenantId).orElse(null);
        if (subscription == null || subscription.getMaxStaff() == null) {
            return;
        }
        long current = personnelDetailsRepository.countByApplicationTenantId(tenantId);
        if (current >= subscription.getMaxStaff()) {
            throw OpsException.conflict("Staff limit reached for this license.");
        }
    }
}
