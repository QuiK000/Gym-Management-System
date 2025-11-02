package com.dev.quikkkk.auth_service.service;

import com.dev.quikkkk.auth_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.auth_service.dto.response.RoleUpdatedResponse;

public interface IUserService {
    RoleUpdatedResponse updateUserRole(String userId, UpdateUserRoleRequest request);
}
