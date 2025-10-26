package com.dev.quikkkk.auth_service.security;

import com.dev.quikkkk.auth_service.service.IJwtService;
import com.dev.quikkkk.auth_service.service.ITokenBlackListService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final IJwtService jwtService;
    private final ITokenBlackListService tokenBlackListService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getServletPath().equals("/api/v1/auth/login") ||
                request.getServletPath().equals("/api/v1/auth/register") ||
                request.getServletPath().equals("/api/v1/auth/logout") ||
                request.getServletPath().equals("/api/v1/auth/verify-email") ||
                request.getServletPath().equals("/api/v1/auth/resend-verification")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        if (tokenBlackListService.isTokenBlacklisted(jwt)) {
            response.setStatus(SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token has been revoked\",\"code\":\"TOKEN_BLACKLISTED\"}");
            return;
        }

        String email = jwtService.extractEmail(jwt);
        String userId = jwtService.extractUserId(jwt);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtService.isTokenValid(jwt, email)) {
                List<String> roles = jwtService.extractRoles(jwt);
                List<SimpleGrantedAuthority> authorities = (roles != null) ?
                        roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList() : List.of();

                var principal = new UserPrincipal(userId, email, authorities);
                var authenticationToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
