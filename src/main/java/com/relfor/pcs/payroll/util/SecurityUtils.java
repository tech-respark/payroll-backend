package com.relfor.pcs.payroll.util;

import com.relfor.pcs.payroll.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static Long getTenantId(Long fallbackTenantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            Long tenantId = ((CustomUserDetails) auth.getPrincipal()).getTenantId();
            if (tenantId != null && tenantId > 0) {
                return tenantId;
            }
        }
        return fallbackTenantId;
    }

    public static Long getStoreId(Long fallbackStoreId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            Long storeId = ((CustomUserDetails) auth.getPrincipal()).getStoreId();
            if (storeId != null && storeId > 0) {
                return storeId;
            }
        }
        return fallbackStoreId;
    }
}
