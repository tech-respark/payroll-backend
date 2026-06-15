package com.relfor.pcs.payroll.security;

import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PersonnelDetailsRepository personnelRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PersonnelDetails personnel = personnelRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        // TODO: Fetch roles from StoreStaffRole and Role tables using personnel.getId()
        return new CustomUserDetails(personnel, new ArrayList<>());
    }
}
