package com.relfor.pcs.payroll.service.ops;

import com.relfor.pcs.payroll.dto.LoginRequest;
import com.relfor.pcs.payroll.dto.LoginResponse;
import com.relfor.pcs.payroll.entity.PlatformUser;
import com.relfor.pcs.payroll.repository.PlatformUserRepository;
import com.relfor.pcs.payroll.security.CustomUserDetails;
import com.relfor.pcs.payroll.security.JwtUtil;
import com.relfor.pcs.payroll.security.PlatformUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpsAuthService {

    private final PlatformUserRepository platformUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformUserDetailsService platformUserDetailsService;
    private final JwtUtil jwtUtil;

    public OpsAuthService(
            PlatformUserRepository platformUserRepository,
            PasswordEncoder passwordEncoder,
            PlatformUserDetailsService platformUserDetailsService,
            JwtUtil jwtUtil) {
        this.platformUserRepository = platformUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.platformUserDetailsService = platformUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        LoginResponse response = new LoginResponse();
        if (request.getUsername() == null || request.getPassword() == null) {
            response.setSuccess(false);
            response.setMessage("Username and password are required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        PlatformUser user = platformUserRepository.findByUsernameIgnoreCase(request.getUsername()).orElse(null);
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())
                || !Boolean.TRUE.equals(user.getActive())) {
            response.setSuccess(false);
            response.setMessage("Invalid username or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        CustomUserDetails details = (CustomUserDetails) platformUserDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generatePlatformToken(details);
        List<String> roles = details.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .toList();

        response.setSuccess(true);
        response.setMessage("Login successful.");
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setFirstName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setActive(true);
        response.setRoles(roles);
        response.setPrincipalType(CustomUserDetails.PRINCIPAL_PLATFORM);
        response.setOpsRole(user.getRole().name());
        return ResponseEntity.ok(response);
    }
}
