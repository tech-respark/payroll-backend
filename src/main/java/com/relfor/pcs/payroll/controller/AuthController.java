package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.LoginRequest;
import com.relfor.pcs.payroll.dto.LoginResponse;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.TenantSubscription;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.PlatformUserRepository;
import com.relfor.pcs.payroll.repository.TenantSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityNames;
import com.relfor.pcs.payroll.activity.ActivityOutcome;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityResourceTypes;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.relfor.pcs.payroll.util.SecurityUtils;
import com.relfor.pcs.payroll.dto.GenerateOtpRequest;
import com.relfor.pcs.payroll.dto.ResetPasswordRequest;
import com.relfor.pcs.payroll.dto.VerifyOtpRequest;
import com.relfor.pcs.payroll.dto.Verify2faRequest;
import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.security.CustomUserDetails;
import com.relfor.pcs.payroll.security.CustomUserDetailsService;
import com.relfor.pcs.payroll.security.JwtUtil;
import com.relfor.pcs.payroll.service.payroll.TotpLoginService;

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

    @Autowired
    private ActivityRecorder activityRecorder;

    @Autowired
    private TenantSubscriptionRepository tenantSubscriptionRepository;

    @Autowired
    private PlatformUserRepository platformUserRepository;

    @Autowired
    private TotpLoginService totpLoginService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();
        
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            response.setSuccess(false);
            response.setMessage("Username and password are required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (platformUserRepository.findByUsernameIgnoreCase(loginRequest.getUsername()).isPresent()) {
            response.setSuccess(false);
            response.setMessage("Invalid username/email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
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
                if (personnel.getActive() == null || !personnel.getActive()) {
                    response.setSuccess(false);
                    response.setMessage("Invalid username/email or password.");
                    recordLoginFailure(personnel, loginRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
                if (!subscriptionAllowsLogin(personnel.getApplicationTenantId())) {
                    response.setSuccess(false);
                    response.setMessage("Invalid username/email or password.");
                    recordLoginFailure(personnel, loginRequest.getUsername());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                if (totpLoginService.isEnabled(personnel)) {
                    response.setSuccess(true);
                    response.setRequires2fa(true);
                    response.setChallengeToken(totpLoginService.createChallenge(personnel.getId()));
                    response.setMessage("Enter the 6-digit code from your authenticator app.");
                    response.setUsername(personnel.getUsername());
                    return ResponseEntity.ok(response);
                }
                return ResponseEntity.ok(completeLogin(personnel));
            }
        }

        response.setSuccess(false);
        response.setMessage("Invalid username/email or password.");
        recordLoginFailure(personnelOptional.orElse(null), loginRequest.getUsername());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/login/verify-2fa")
    public ResponseEntity<LoginResponse> verify2fa(@RequestBody Verify2faRequest request) {
        LoginResponse response = new LoginResponse();
        try {
            PersonnelDetails personnel = totpLoginService.consumeChallenge(request.getChallengeToken(), request.getOtp());
            if (personnel.getActive() == null || !personnel.getActive() || !subscriptionAllowsLogin(personnel.getApplicationTenantId())) {
                response.setSuccess(false);
                response.setMessage("Invalid username/email or password.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            return ResponseEntity.ok(completeLogin(personnel));
        } catch (IllegalArgumentException ex) {
            response.setSuccess(false);
            response.setMessage(ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    private LoginResponse completeLogin(PersonnelDetails personnel) {
        personnel.setLastLoginAt(Instant.now());
        personnelDetailsRepository.save(personnel);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(personnel.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toList());
        LoginResponse response = new LoginResponse();
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
        activityRecorder.recordSafely(ActivityEvent.builder()
                .action(ActivityActions.AUTH_LOGIN_SUCCESS)
                .outcome(ActivityOutcome.SUCCESS)
                .actorType(com.relfor.pcs.payroll.activity.ActivityActorType.USER)
                .actorStaffId(personnel.getId())
                .actorUsername(personnel.getUsername())
                .actorDisplayName(ActivityNames.displayName(personnel))
                .tenantId(personnel.getApplicationTenantId())
                .storeId(personnel.getStoreId())
                .resourceType(ActivityResourceTypes.SESSION)
                .summary("Signed in")
                .build());
        return response;
    }

    private boolean subscriptionAllowsLogin(Long tenantId) {
        if (tenantId == null) {
            return false;
        }
        return tenantSubscriptionRepository.findByTenantId(tenantId)
                .map(TenantSubscription::allowsClientLogin)
                .orElse(true);
    }

    private void recordLoginFailure(PersonnelDetails personnel, String attemptedUsername) {
        var builder = ActivityEvent.builder()
                .action(ActivityActions.AUTH_LOGIN_FAILURE)
                .outcome(ActivityOutcome.FAILURE)
                .actorType(com.relfor.pcs.payroll.activity.ActivityActorType.USER)
                .resourceType(ActivityResourceTypes.SESSION)
                .summary("Sign-in failed");
        if (personnel != null) {
            String name = ActivityNames.displayName(personnel);
            builder.actorStaffId(personnel.getId())
                    .actorUsername(personnel.getUsername())
                    .actorDisplayName(name)
                    .tenantId(personnel.getApplicationTenantId())
                    .storeId(personnel.getStoreId())
                    .summary("Sign-in failed" + (name != null ? " for " + name : ""));
        } else {
            builder.actorUsername(attemptedUsername).tenantId(0L);
        }
        activityRecorder.recordSafely(builder.build());
    }

    @GetMapping("/session")
    public ResponseEntity<LoginResponse> currentSession() {
        LoginResponse response = new LoginResponse();
        Long staffId = SecurityUtils.getStaffId();
        if (staffId == null) {
            response.setSuccess(false);
            response.setMessage("Not authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        Optional<PersonnelDetails> personnel = personnelDetailsRepository.findById(staffId);
        if (personnel.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Not authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(personnel.get().getUsername());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toList());
        response.setSuccess(true);
        response.setRoles(roles);
        response.setStaffId(personnel.get().getId());
        response.setUsername(personnel.get().getUsername());
        response.setFirstName(personnel.get().getFirstName());
        response.setLastName(personnel.get().getLastName());
        response.setTenantId(personnel.get().getApplicationTenantId());
        response.setStoreId(personnel.get().getStoreId());
        response.setActive(personnel.get().getActive() != null ? personnel.get().getActive() : true);
        return ResponseEntity.ok(response);
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