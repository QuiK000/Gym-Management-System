package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.dto.kafka.UserLoginEvent;
import com.dev.quikkkk.auth_service.dto.kafka.UserRegisteredEvent;
import com.dev.quikkkk.auth_service.dto.request.LoginRequest;
import com.dev.quikkkk.auth_service.dto.request.RefreshTokenRequest;
import com.dev.quikkkk.auth_service.dto.request.RegistrationRequest;
import com.dev.quikkkk.auth_service.dto.response.AuthenticationResponse;
import com.dev.quikkkk.auth_service.entity.Role;
import com.dev.quikkkk.auth_service.entity.UserCredentials;
import com.dev.quikkkk.auth_service.exception.BusinessException;
import com.dev.quikkkk.auth_service.mapper.UserMapper;
import com.dev.quikkkk.auth_service.repository.IRoleRepository;
import com.dev.quikkkk.auth_service.repository.IUserCredentialsRepository;
import com.dev.quikkkk.auth_service.service.IAuthenticationService;
import com.dev.quikkkk.auth_service.service.IBruteForceProtectionService;
import com.dev.quikkkk.auth_service.service.IEmailVerificationService;
import com.dev.quikkkk.auth_service.service.IJwtService;
import com.dev.quikkkk.auth_service.service.ITokenBlackListService;
import com.dev.quikkkk.auth_service.utils.NetworkUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.dev.quikkkk.auth_service.exception.ErrorCode.EMAIL_ALREADY_EXISTS;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.INVALID_CREDENTIALS;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.INVALID_TOKEN;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.PASSWORD_MISMATCH;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.TOKEN_REVOKED;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.TOO_MANY_ATTEMPTS;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final static String TOKEN_TYPE = "Bearer ";

    private final IUserCredentialsRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IJwtService jwtService;
    private final IBruteForceProtectionService bruteForceProtectionService;
    private final IEmailVerificationService emailVerificationService;
    private final ITokenBlackListService tokenBlackListService;
    private final UserMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());
        String clientIp = NetworkUtils.getClientIp().orElseThrow(() -> new BusinessException(INTERNAL_SERVER_ERROR));
        if (bruteForceProtectionService.isBlocked(clientIp)) throw new BusinessException(TOO_MANY_ATTEMPTS);

        try {
            UserCredentials userCredentials = findUserByEmail(request.getEmail());
            if (!userCredentials.isEnabled()) {
                log.warn("User {} is disabled", request.getEmail());
                throw new BusinessException(INVALID_CREDENTIALS);
            }

            if (userCredentials.isLocked()) {
                log.warn("User {} is locked", request.getEmail());
                throw new BusinessException(INVALID_CREDENTIALS);
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            bruteForceProtectionService.registerSuccessfulAttempt(clientIp);
            CompletableFuture<String> accessTokenFuture = CompletableFuture.supplyAsync(
                    () -> jwtService.generateAccessToken(userCredentials));
            CompletableFuture<String> refreshTokenFuture = CompletableFuture.supplyAsync(
                    () -> jwtService.generateRefreshToken(userCredentials));

            String accessToken = accessTokenFuture.get();
            String refreshToken = refreshTokenFuture.get();

            log.info("User {} logged in successfully", request.getEmail());
            UserLoginEvent event = UserLoginEvent.builder()
                    .userId(userCredentials.getId())
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Sending login event to kafka topic");
            kafkaTemplate.send("user-login-topic", event);

            log.info("Login response: {}", accessToken);
            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType(TOKEN_TYPE)
                    .build();
        } catch (Exception e) {
            bruteForceProtectionService.registerFailedAttempt(clientIp);
            int remainingAttempts = bruteForceProtectionService.getRemainingAttempts(clientIp);

            log.warn(
                    "Failed login attempt for user: {} from IP: {}. Remaining attempts: {}",
                    request.getEmail(), clientIp, remainingAttempts
            );

            throw new BusinessException(INVALID_CREDENTIALS);
        }
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request: {}", request);
        String newAccessToken = jwtService.refreshAccessToken(request.getRefreshToken());

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType(TOKEN_TYPE)
                .build();
    }

    @Override
    @Transactional
    public void register(RegistrationRequest request) {
        log.info("Registration request: {}", request);

        checkUserEmail(request.getEmail());
        checkPasswords(request.getPassword(), request.getConfirmPassword());

        Role defaultRole = roleRepository.findByName("ROLE_MEMBER")
                .orElseThrow(() -> new EntityNotFoundException("Role user does not exist"));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        UserCredentials userCredentials = mapper.toUser(request);

        userCredentials.setRoles(roles);
        userCredentials.setEnabled(false);
        userCredentials.setEmailVerified(false);

        log.debug("Saving user: {}", userCredentials);
        userRepository.save(userCredentials);
        log.info("User {} registered", userCredentials.getEmail());

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(userCredentials.getId())
                .email(userCredentials.getEmail())
                .role(defaultRole.getName())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("user-registered-topic", event);
        log.info("User registered event sent for user: {}", userCredentials.getId());

        String ipAddress = NetworkUtils.getClientIp().orElseThrow(() -> new BusinessException(INTERNAL_SERVER_ERROR));
        emailVerificationService.sendVerificationCode(
                userCredentials.getId(),
                userCredentials.getEmail(),
                ipAddress
        );
    }

    @Override
    public void logout(String token) {
        log.info("Logging out user with token: {}", token);

        String actualToken = token.startsWith(TOKEN_TYPE) ? token.substring(TOKEN_TYPE.length()).trim() : token;
        tokenBlackListService.blacklistToken(actualToken);

        log.info("User logged out successfully");
    }

    @Override
    public Map<String, Object> validateToken(String token) {
        log.info("Validating token: {}", token);

        if (tokenBlackListService.isTokenBlacklisted(token)) {
            log.warn("Token validation failed: Token is blacklisted");
            throw new BusinessException(TOKEN_REVOKED);
        }

        try {
            String email = jwtService.extractEmail(token);
            String userId = jwtService.extractUserId(token);
            List<String> roles = jwtService.extractRoles(token);

            if (!jwtService.isTokenValid(token, email)) {
                log.warn("Token validation failed: Token is invalid or expired");
                throw new BusinessException(INVALID_TOKEN);
            }

            Map<String, Object> result = new HashMap<>();

            result.put("valid", true);
            result.put("userId", userId);
            result.put("email", email);
            result.put("roles", roles);
            result.put("tokenType", extractTokenType(token));

            log.info("Token validation successful for user: {}", userId);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token validation failed with exception: {}", e.getMessage());
            throw new BusinessException(INVALID_TOKEN);
        }
    }

    @Override
    public UserCredentials findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    private void checkUserEmail(String email) {
        boolean emailExists = userRepository.existsByEmailIgnoreCase(email);
        if (emailExists) throw new BusinessException(EMAIL_ALREADY_EXISTS);
    }

    private void checkPasswords(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) throw new BusinessException(PASSWORD_MISMATCH);
    }

    private String extractTokenType(String token) {
        try {
            return "ACCESS_TOKEN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
