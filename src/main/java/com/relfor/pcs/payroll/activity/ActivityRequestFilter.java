package com.relfor.pcs.payroll.activity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class ActivityRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlation = firstHeader(request, "X-Request-Id", "X-Correlation-Id");
        if (correlation == null || correlation.isBlank()) {
            correlation = UUID.randomUUID().toString();
        }
        ActivityRequestContext.set(new ActivityRequestContext.Snapshot(
                clientIp(request),
                trim(request.getHeader("User-Agent"), 255),
                trim(correlation, 64),
                request.getMethod(),
                trim(request.getRequestURI(), 255)
        ));
        try {
            filterChain.doFilter(request, response);
        } finally {
            ActivityRequestContext.clear();
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            String first = comma > 0 ? forwarded.substring(0, comma) : forwarded;
            return normalizeIp(trim(first, 45));
        }
        return normalizeIp(trim(request.getRemoteAddr(), 45));
    }

    static String normalizeIp(String ip) {
        if (ip == null) {
            return null;
        }
        if ("https://example.net/id/garnet".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    private static String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}
