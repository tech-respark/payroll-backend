package com.relfor.pcs.payroll.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration-ms:36000000}")
    private long expirationTime;

    private SecretKey key;

    @PostConstruct
    void initSigningKey() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret must be configured and at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractPrincipalType(String token) {
        String value = extractClaim(token, claims -> claims.get("principalType", String.class));
        return value == null || value.isBlank() ? CustomUserDetails.PRINCIPAL_CLIENT : value;
    }

    public String generateToken(CustomUserDetails userDetails) {
        return generateToken(userDetails, expirationTime);
    }

    public String generateToken(CustomUserDetails userDetails, long expirationMs) {
        return generateToken(userDetails, expirationMs, Map.of());
    }

    public String generateToken(CustomUserDetails userDetails, long expirationMs, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("principalType", CustomUserDetails.PRINCIPAL_CLIENT);
        claims.put("tenantId", userDetails.getTenantId());
        claims.put("storeId", userDetails.getStoreId());
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }
        return createToken(claims, userDetails.getUsername(), expirationMs);
    }

    public String extractSupportSessionId(String token) {
        return extractClaim(token, claims -> claims.get("supportSessionId", String.class));
    }

    public String generatePlatformToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("principalType", CustomUserDetails.PRINCIPAL_PLATFORM);
        if (userDetails.getOpsRole() != null) {
            claims.put("opsRole", userDetails.getOpsRole().name());
        }
        return createToken(claims, userDetails.getUsername(), expirationTime);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Boolean validateToken(String token, CustomUserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
