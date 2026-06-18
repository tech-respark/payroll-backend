package com.relfor.pcs.payroll.security;

import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PersonnelDetailsRepository personnelRepository;
    
    @Autowired
    private com.relfor.pcs.payroll.repository.StoreStaffRoleRepository storeStaffRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PersonnelDetails personnel = personnelRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        // Fetch assigned roles for this staff based on their personnelCode
        List<com.relfor.pcs.payroll.entity.Role> roles = storeStaffRoleRepository.findActiveRolesByStaffId(personnel.getPersonnelCode());
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        for (com.relfor.pcs.payroll.entity.Role role : roles) {
            // Add the role itself
            if (role.getName() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase().replace(" ", "_")));
            }
            
            // Add all permissions associated with this role
            if (role.getPermissions() != null && !role.getPermissions().trim().isEmpty()) {
                String[] perms = role.getPermissions().split(",");
                for (String perm : perms) {
                    authorities.add(new SimpleGrantedAuthority(perm.trim()));
                }
            }
        }

        return new CustomUserDetails(personnel, authorities);
    }
}
