package com.dev.quikkkk.auth_service.service;

import com.dev.quikkkk.auth_service.dto.request.LoginRequest;
import com.dev.quikkkk.auth_service.dto.request.RegistrationRequest;
import com.dev.quikkkk.auth_service.dto.response.AuthenticationResponse;
import com.dev.quikkkk.auth_service.entity.UserCredentials;

public interface IAuthenticationService {
    AuthenticationResponse login(LoginRequest request);

    void register(RegistrationRequest request);

    UserCredentials findUserByEmail(String email);
}
