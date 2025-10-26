package com.dev.quikkkk.auth_service.security;

import com.dev.quikkkk.auth_service.service.IJwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenFilter extends OncePerRequestFilter {
    private final IJwtService service;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!request.getServletPath().equals("/api/v1/auth/refresh-token")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Refresh token required\",\"code\":\"UNAUTHORIZED\"}");
            return;
        }

        try {
            String refreshToken = authHeader.substring(7);
            String email = service.extractEmail(refreshToken);

            if (email != null && isRefreshToken(refreshToken)) {
                var authentication = new UsernamePasswordAuthenticationToken(email, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                response.setStatus(SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid refresh token\",\"code\":\"INVALID_TOKEN\"}");
                return;
            }
        } catch (Exception e) {
            log.error("Error validating refresh token", e);
            response.setStatus(SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid refresh token\",\"code\":\"INVALID_TOKEN\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRefreshToken(String token) {
        try {
            return "REFRESH_TOKEN".equals(service.extractTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }
}
