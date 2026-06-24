package com.relfor.pcs.payroll.security;

import com.relfor.pcs.payroll.entity.PersonnelDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final PersonnelDetails personnel;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(PersonnelDetails personnel, Collection<? extends GrantedAuthority> authorities) {
        this.personnel = personnel;
        this.authorities = authorities;
    }

    public Long getTenantId() {
        return personnel.getApplicationTenantId();
    }

    public Long getStoreId() {
        return personnel.getStoreId();
    }
    
    public Long getStaffId() {
        return personnel.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return personnel.getPassword();
    }

    @Override
    public String getUsername() {
        return personnel.getUsername();
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
        return true;
    }
}
