package com.dev.quikkkk.user_service.client;

import com.dev.quikkkk.user_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.user_service.dto.response.ApiResponse;
import com.dev.quikkkk.user_service.dto.response.RoleUpdatedResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", url = "{app.config.auth-service-url}")
public interface IAuthClient {
    @PutMapping("/internal/users/{user-id}/role")
    ApiResponse<RoleUpdatedResponse> updateUserRole(
            @PathVariable("user-id") String userId,
            @RequestBody @Valid UpdateUserRoleRequest request
    );
}
