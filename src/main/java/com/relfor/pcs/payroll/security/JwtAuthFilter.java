package com.relfor.pcs.payroll.security;

import com.relfor.pcs.payroll.repository.SupportSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PlatformUserDetailsService platformUserDetailsService;

    @Autowired
    private SupportSessionRepository supportSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        String principalType = CustomUserDetails.PRINCIPAL_CLIENT;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                principalType = jwtUtil.extractPrincipalType(jwt);
            } catch (Exception e) {
                logger.error("Invalid JWT Token");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = CustomUserDetails.PRINCIPAL_PLATFORM.equals(principalType)
                    ? platformUserDetailsService.loadUserByUsername(username)
                    : this.userDetailsService.loadUserByUsername(username);

            if (userDetails.isEnabled() && jwtUtil.validateToken(jwt, (CustomUserDetails) userDetails)
                    && supportSessionActive(jwt)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean supportSessionActive(String jwt) {
        String sessionId = jwtUtil.extractSupportSessionId(jwt);
        if (sessionId == null || sessionId.isBlank()) {
            return true;
        }
        return supportSessionRepository.findByTokenId(sessionId)
                .map(session -> session.isActive(java.time.Instant.now()))
                .orElse(false);
    }
}
