package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.auth_service.dto.response.RoleUpdatedResponse;
import com.dev.quikkkk.auth_service.entity.Role;
import com.dev.quikkkk.auth_service.exception.BusinessException;
import com.dev.quikkkk.auth_service.mapper.UserMapper;
import com.dev.quikkkk.auth_service.repository.IRoleRepository;
import com.dev.quikkkk.auth_service.repository.IUserCredentialsRepository;
import com.dev.quikkkk.auth_service.service.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.dev.quikkkk.auth_service.exception.ErrorCode.ROLE_NOT_FOUND;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {
    private final IUserCredentialsRepository userCredentialsRepository;
    private final IRoleRepository roleRepository;
    private final UserMapper mapper;

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public RoleUpdatedResponse updateUserRole(String userId, UpdateUserRoleRequest request) {
        log.info("Updating role for user: {} to role: {}", userId, request.getRole());

        var user = userCredentialsRepository.findById(userId).orElseThrow(() -> {
                    log.error("User: {} does not exist", userId);
                    return new BusinessException(USER_NOT_FOUND);
                });

        var newRole = roleRepository.findByName(request.getRole().toString()).orElseThrow(() -> {
                    log.error("Role: {} does not exist", request.getRole());
                    return new BusinessException(ROLE_NOT_FOUND);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(newRole);
        user.setRoles(roles);

        var updatedUser = userCredentialsRepository.save(user);

        log.info("Role updated successfully for user: {}", userId);

        return RoleUpdatedResponse.builder()
                .userId(updatedUser.getId())
                .role(newRole.getName())
                .build();
    }
}
