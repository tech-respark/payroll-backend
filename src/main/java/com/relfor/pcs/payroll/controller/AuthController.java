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

import org.springframework.security.crypto.password.PasswordEncoder;
import com.relfor.pcs.payroll.dto.GenerateOtpRequest;
import com.relfor.pcs.payroll.dto.ResetPasswordRequest;
import com.relfor.pcs.payroll.dto.VerifyOtpRequest;
import com.relfor.pcs.payroll.dto.ResponseModel;
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
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.relfor.pcs.payroll.service.AuthService authService;

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
            // Validate password matching strictly with BCrypt
            boolean isPasswordValid = false;
            if (personnel.getPassword() != null) {
                if (passwordEncoder.matches(loginRequest.getPassword(), personnel.getPassword())) {
                    isPasswordValid = true;
                }
            }

            if (isPasswordValid) {
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
				response.setStaffId(personnel.getId());
                
                return ResponseEntity.ok(response);
            }
        }

        response.setSuccess(false);
        response.setMessage("Invalid username/email or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/forgot-password/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody GenerateOtpRequest request) {
        ResponseModel response = authService.generateOtp(request.getUsernameOrMobile());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        ResponseModel response = authService.verifyOtp(request.getUsernameOrMobile(), request.getOtp());
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/forgot-password/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        ResponseModel response = authService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ResponseEntity.status(response.getCode()).body(response);
    }
}