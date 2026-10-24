package com.relfor.pcs.payroll.security;

import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.PlatformUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    public static final String PRINCIPAL_CLIENT = "CLIENT";
    public static final String PRINCIPAL_PLATFORM = "PLATFORM";

    private final String principalType;
    private final PersonnelDetails personnel;
    private final PlatformUser platformUser;
    private final Collection<? extends GrantedAuthority> authorities;

    private CustomUserDetails(
            String principalType,
            PersonnelDetails personnel,
            PlatformUser platformUser,
            Collection<? extends GrantedAuthority> authorities) {
        this.principalType = principalType;
        this.personnel = personnel;
        this.platformUser = platformUser;
        this.authorities = authorities;
    }

    public static CustomUserDetails fromPersonnel(
            PersonnelDetails personnel,
            Collection<? extends GrantedAuthority> authorities) {
        return new CustomUserDetails(PRINCIPAL_CLIENT, personnel, null, authorities);
    }

    public static CustomUserDetails fromPlatform(
            PlatformUser platformUser,
            Collection<? extends GrantedAuthority> authorities) {
        return new CustomUserDetails(PRINCIPAL_PLATFORM, null, platformUser, authorities);
    }

    public boolean isPlatform() {
        return PRINCIPAL_PLATFORM.equals(principalType);
    }

    public String getPrincipalType() {
        return principalType;
    }

    public PlatformUser.Role getOpsRole() {
        return platformUser != null ? platformUser.getRole() : null;
    }

    public Long getTenantId() {
        return personnel != null ? personnel.getApplicationTenantId() : null;
    }

    public Long getStoreId() {
        return personnel != null ? personnel.getStoreId() : null;
    }

    public Long getStaffId() {
        return personnel != null ? personnel.getId() : null;
    }

    public PlatformUser getPlatformUser() {
        return platformUser;
    }

    public PersonnelDetails getPersonnel() {
        return personnel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return platformUser != null ? platformUser.getPasswordHash() : personnel.getPassword();
    }

    @Override
    public String getUsername() {
        return platformUser != null ? platformUser.getUsername() : personnel.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (platformUser != null) {
            return Boolean.TRUE.equals(platformUser.getActive());
        }
        return personnel.getActive() != null && personnel.getActive();
    }
}
