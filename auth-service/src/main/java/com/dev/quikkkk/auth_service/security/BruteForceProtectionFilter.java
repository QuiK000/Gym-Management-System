package com.dev.quikkkk.auth_service.security;

import com.dev.quikkkk.auth_service.service.IBruteForceProtectionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;

@Component
@RequiredArgsConstructor
@Slf4j
public class BruteForceProtectionFilter extends OncePerRequestFilter {
    private final IBruteForceProtectionService service;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String clientIp = getClientIp(request);

        log.info("Request URI: {}, Client IP: {}", requestUri, clientIp);

        if (requestUri.equals("/api/v1/auth/login") || requestUri.equals("/api/v1/auth/register")) {
            if (service.isBlocked(clientIp)) {
                log.warn("Blocked request from IP: {} to {}", clientIp, requestUri);

                response.setStatus(SC_REQUEST_ENTITY_TOO_LARGE);
                response.getWriter().write("Too many requests. Please try again later.");

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();

        return ip;
    }
}
