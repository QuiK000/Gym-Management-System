package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.auth_service.dto.response.RoleUpdatedResponse;
import com.dev.quikkkk.auth_service.exception.BusinessException;
import com.dev.quikkkk.auth_service.exception.ErrorCode;
import com.dev.quikkkk.auth_service.mapper.UserMapper;
import com.dev.quikkkk.auth_service.repository.IUserCredentialsRepository;
import com.dev.quikkkk.auth_service.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final IUserCredentialsRepository repository;
    private final UserMapper mapper;

    @Override
    public RoleUpdatedResponse updateUserRole(String userId, UpdateUserRoleRequest request) {
        var user = repository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        var updatedUser = mapper.updateUserRole(user, request);
        return null;
    }
}
