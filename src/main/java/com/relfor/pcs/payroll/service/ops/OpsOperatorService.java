package com.relfor.pcs.payroll.service.ops;

import com.relfor.pcs.payroll.entity.PlatformUser;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.PlatformUserRepository;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpsOperatorService {

    private final PlatformUserRepository platformUserRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final PasswordEncoder passwordEncoder;

    public OpsOperatorService(
            PlatformUserRepository platformUserRepository,
            PersonnelDetailsRepository personnelDetailsRepository,
            PasswordEncoder passwordEncoder) {
        this.platformUserRepository = platformUserRepository;
        this.personnelDetailsRepository = personnelDetailsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Map<String, Object>> list() {
        return platformUserRepository.findAllByOrderByIdAsc().stream().map(this::toView).toList();
    }

    public List<Map<String, Object>> listSalesOwners() {
        return platformUserRepository.findAllByOrderByIdAsc().stream()
                .filter((user) -> Boolean.TRUE.equals(user.getActive()))
                .map(this::toOwnerView)
                .toList();
    }

    public Map<String, Object> create(Map<String, Object> body) {
        String username = required(body, "username");
        String password = required(body, "password");
        PlatformUser.Role role = parseRole(body.get("role"));
        if (password.length() < 8) {
            throw OpsException.badRequest("Password must be at least 8 characters.");
        }
        if (platformUserRepository.findByUsernameIgnoreCase(username).isPresent()
                || personnelDetailsRepository.findByUsername(username).isPresent()) {
            throw OpsException.conflict("Username is already taken.");
        }
        PlatformUser user = new PlatformUser();
        user.setUsername(username.trim());
        user.setEmail(stringVal(body.get("email")));
        user.setDisplayName(stringVal(body.get("displayName")));
        user.setRole(role);
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(password));
        return toView(platformUserRepository.save(user));
    }

    public Map<String, Object> update(Long id, Map<String, Object> body) {
        PlatformUser user = platformUserRepository.findById(id)
                .orElseThrow(() -> OpsException.notFound("Operator not found."));
        if (body.containsKey("displayName")) {
            user.setDisplayName(stringVal(body.get("displayName")));
        }
        if (body.containsKey("email")) {
            user.setEmail(stringVal(body.get("email")));
        }
        if (body.containsKey("role")) {
            user.setRole(parseRole(body.get("role")));
        }
        if (body.containsKey("password") && body.get("password") != null
                && !String.valueOf(body.get("password")).isBlank()) {
            String password = String.valueOf(body.get("password"));
            if (password.length() < 8) {
                throw OpsException.badRequest("Password must be at least 8 characters.");
            }
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        if (body.containsKey("active")) {
            boolean active = Boolean.TRUE.equals(body.get("active"))
                    || "true".equalsIgnoreCase(String.valueOf(body.get("active")));
            if (!active && user.getRole() == PlatformUser.Role.SUPERADMIN
                    && platformUserRepository.countByRoleAndActiveTrue(PlatformUser.Role.SUPERADMIN) <= 1) {
                throw OpsException.conflict("The last active superadmin cannot be deactivated.");
            }
            user.setActive(active);
        }
        return toView(platformUserRepository.save(user));
    }

    public Map<String, Object> changeOwnPassword(Map<String, Object> body) {
        Long id = SecurityUtils.currentPlatformUserId();
        if (id == null) {
            throw OpsException.forbidden("A platform session is required.");
        }
        PlatformUser user = platformUserRepository.findById(id)
                .orElseThrow(() -> OpsException.notFound("Operator not found."));
        String current = required(body, "currentPassword");
        String next = required(body, "password");
        if (next.length() < 8) {
            throw OpsException.badRequest("Password must be at least 8 characters.");
        }
        if (!passwordEncoder.matches(current, user.getPasswordHash())) {
            throw OpsException.badRequest("Current password is not correct.");
        }
        user.setPasswordHash(passwordEncoder.encode(next));
        platformUserRepository.save(user);
        return Map.of("updated", true);
    }

    private Map<String, Object> toView(PlatformUser user) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", user.getId());
        view.put("username", user.getUsername());
        view.put("email", user.getEmail());
        view.put("displayName", user.getDisplayName());
        view.put("role", user.getRole().name());
        view.put("active", user.getActive());
        return view;
    }

    private Map<String, Object> toOwnerView(PlatformUser user) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", user.getId());
        view.put("username", user.getUsername());
        view.put("displayName", user.getDisplayName() != null && !user.getDisplayName().isBlank()
                ? user.getDisplayName()
                : user.getUsername());
        view.put("role", user.getRole().name());
        return view;
    }

    private PlatformUser.Role parseRole(Object raw) {
        if (raw == null) {
            throw OpsException.badRequest("Role is required.");
        }
        try {
            return PlatformUser.Role.valueOf(String.valueOf(raw).trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw OpsException.badRequest("Role must be SUPERADMIN or SALES.");
        }
    }

    private String required(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw OpsException.badRequest(key + " is required.");
        }
        return String.valueOf(value).trim();
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
