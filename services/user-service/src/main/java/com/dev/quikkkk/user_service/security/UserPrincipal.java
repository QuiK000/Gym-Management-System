package com.dev.quikkkk.user_service.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record UserPrincipal(
        String id,
        String email,
        Collection<? extends GrantedAuthority> authorities
) {
}
