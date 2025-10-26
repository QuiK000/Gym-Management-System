package com.dev.quikkkk.auth_service.service;

import com.dev.quikkkk.auth_service.dto.request.LoginRequest;
import com.dev.quikkkk.auth_service.dto.request.RefreshTokenRequest;
import com.dev.quikkkk.auth_service.dto.request.RegistrationRequest;
import com.dev.quikkkk.auth_service.dto.response.AuthenticationResponse;
import com.dev.quikkkk.auth_service.entity.UserCredentials;

import java.util.Map;

public interface IAuthenticationService {
    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse refreshToken(RefreshTokenRequest request);

    void register(RegistrationRequest request);

    void logout(String token);

    void forgotPassword(String email);

    Map<String, Object> validateToken(String token);

    UserCredentials findUserByEmail(String email);
}
