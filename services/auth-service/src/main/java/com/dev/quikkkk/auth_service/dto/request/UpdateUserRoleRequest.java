package com.dev.quikkkk.auth_service.dto.request;

import com.dev.quikkkk.auth_service.enums.RoleTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRoleRequest {
    private RoleTypes role;
}
