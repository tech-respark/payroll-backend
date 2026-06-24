package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.LoginRequest;
import com.relfor.pcs.payroll.dto.LoginResponse;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import com.relfor.pcs.payroll.security.CustomUserDetails;
import com.relfor.pcs.payroll.security.CustomUserDetailsService;
import com.relfor.pcs.payroll.security.JwtUtil;

@RestController
@RequestMapping("/payroll-management/v1")
public class AuthController {

    @Autowired
    private PersonnelDetailsRepository personnelDetailsRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();
        
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            response.setSuccess(false);
            response.setMessage("Username and password are required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Try looking up by username
        Optional<PersonnelDetails> personnelOptional = personnelDetailsRepository.findByUsername(loginRequest.getUsername());
        
        // If not found, try looking up by email
        if (!personnelOptional.isPresent()) {
            personnelOptional = personnelDetailsRepository.findByEmail(loginRequest.getUsername());
        }

        if (personnelOptional.isPresent()) {
            PersonnelDetails personnel = personnelOptional.get();
            // Validate password matching
            if (loginRequest.getPassword().equals(personnel.getPassword())) {
                CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(personnel.getUsername());
                
                String token = jwtUtil.generateToken(userDetails);
                List<String> roles = userDetails.getAuthorities().stream()
                        .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                        .collect(java.util.stream.Collectors.toList());

                response.setSuccess(true);
                response.setMessage("Login successful.");
                response.setToken(token);
                response.setRoles(roles);
                response.setUsername(personnel.getUsername());
                response.setFirstName(personnel.getFirstName());
                response.setLastName(personnel.getLastName());
                response.setDesignation(personnel.getDesignation());
                response.setEmail(personnel.getEmail());
                response.setStoreId(personnel.getStoreId());
                response.setTenantId(personnel.getApplicationTenantId());
                response.setActive(personnel.getActive() != null ? personnel.getActive() : true);
                
                return ResponseEntity.ok(response);
            }
        }

        response.setSuccess(false);
        response.setMessage("Invalid username/email or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}