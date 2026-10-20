package com.relfor.pcs.payroll.util;

import com.relfor.pcs.payroll.security.CustomUserDetails;
import com.relfor.pcs.payroll.security.Permissions;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public static Long getStaffId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getStaffId();
        }
        return null;
    }

    public static boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || authority == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> authority.equals(a.getAuthority()));
    }

    public static void requireSelfOrAnyAuthority(Long requestedStaffId, String... authorities) {
        if (requestedStaffId == null) {
            throw new AccessDeniedException("Staff id is required");
        }
        Long self = getStaffId();
        if (self != null && self.equals(requestedStaffId)) {
            return;
        }
        if (authorities != null) {
            for (String authority : authorities) {
                if (hasAuthority(authority)) {
                    return;
                }
            }
        }
        throw new AccessDeniedException("Not allowed to access another staff record");
    }

    public static List<Long> constrainStaffIds(List<Long> requestedStaffIds) {
        if (hasAuthority(Permissions.MANAGE_ATTENDANCE)
                || hasAuthority(Permissions.MANAGE_STAFF)
                || hasAuthority(Permissions.VIEW_OTHER_STAFF)) {
            return requestedStaffIds;
        }
        Long self = getStaffId();
        if (self == null) {
            throw new AccessDeniedException("Authenticated staff id is required");
        }
        if (requestedStaffIds == null || requestedStaffIds.isEmpty()) {
            List<Long> selfOnly = new ArrayList<>();
            selfOnly.add(self);
            return selfOnly;
        }
        List<Long> allowed = requestedStaffIds.stream().filter(id -> Objects.equals(self, id)).toList();
        if (allowed.isEmpty()) {
            throw new AccessDeniedException("Not allowed to access another staff record");
        }
        return allowed;
    }

    public static boolean canViewOtherStaff() {
        return hasAuthority(Permissions.MANAGE_STAFF) || hasAuthority(Permissions.VIEW_OTHER_STAFF);
    }

    public static boolean hasAnyAuthority(String... authorities) {
        if (authorities == null) {
            return false;
        }
        for (String authority : authorities) {
            if (hasAuthority(authority)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOperationsUser() {
        return hasAnyAuthority(
                Permissions.VIEW_OTHER_STAFF,
                Permissions.MANAGE_STAFF,
                Permissions.MANAGE_ROLES,
                Permissions.MANAGE_SHIFTS,
                Permissions.MANAGE_ATTENDANCE,
                Permissions.MANAGE_SALARY,
                Permissions.MANAGE_LEAVES,
                Permissions.VIEW_REPORTS,
                Permissions.VIEW_HR_DASHBOARD,
                Permissions.VIEW_HOSPITALITY,
                Permissions.MANAGE_HOSPITALITY
        );
    }
}
