package com.relfor.pcs.payroll.security;

import com.relfor.pcs.payroll.entity.PlatformUser;
import com.relfor.pcs.payroll.repository.PlatformUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlatformUserDetailsService {

    private final PlatformUserRepository platformUserRepository;

    public PlatformUserDetailsService(PlatformUserRepository platformUserRepository) {
        this.platformUserRepository = platformUserRepository;
    }

    public UserDetails loadUserByUsername(String username) {
        PlatformUser user = platformUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Platform user not found"));
        return CustomUserDetails.fromPlatform(user, authoritiesFor(user));
    }

    public List<GrantedAuthority> authoritiesFor(PlatformUser user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String role = user.getRole().name();
        authorities.add(new SimpleGrantedAuthority(role));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        return authorities;
    }
}
