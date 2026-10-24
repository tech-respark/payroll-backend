package com.relfor.pcs.payroll.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class OpsPrincipalFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean platform = auth != null
                && auth.getPrincipal() instanceof CustomUserDetails details
                && details.isPlatform();

        if (path.startsWith("/payroll-management/v1/ops/")) {
            boolean login = path.equals("/payroll-management/v1/ops/login")
                    || path.startsWith("/payroll-management/v1/ops/support-handoff/");
            if (!login && auth != null && auth.isAuthenticated() && !platform) {
                reject(response, "Ops APIs require a platform session.");
                return;
            }
        } else if (path.startsWith("/payroll-management/v1/")
                && !path.startsWith("/payroll-management/v1/ops/")
                && platform) {
            reject(response, "Platform users cannot call the client payroll APIs.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"" + message + "\"}");
    }
}
