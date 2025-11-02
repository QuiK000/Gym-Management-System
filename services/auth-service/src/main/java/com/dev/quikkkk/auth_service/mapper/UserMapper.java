package com.dev.quikkkk.auth_service.mapper;

import com.dev.quikkkk.auth_service.dto.request.RegistrationRequest;
import com.dev.quikkkk.auth_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.auth_service.entity.Role;
import com.dev.quikkkk.auth_service.entity.UserCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public UserCredentials toUser(RegistrationRequest request) {
        return UserCredentials.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .locked(false)
                .expired(false)
                .emailVerified(false)
                .roles(new HashSet<>())
                .build();
    }

    public void updateRole(Role user, UpdateUserRoleRequest request) {
        user.setName(request.getRole().toString());
    }
}
