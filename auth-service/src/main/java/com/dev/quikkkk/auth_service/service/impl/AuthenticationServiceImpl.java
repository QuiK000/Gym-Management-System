package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.dto.request.LoginRequest;
import com.dev.quikkkk.auth_service.dto.request.RegistrationRequest;
import com.dev.quikkkk.auth_service.dto.response.AuthenticationResponse;
import com.dev.quikkkk.auth_service.entity.Role;
import com.dev.quikkkk.auth_service.entity.UserCredentials;
import com.dev.quikkkk.auth_service.mapper.UserMapper;
import com.dev.quikkkk.auth_service.repository.IRoleRepository;
import com.dev.quikkkk.auth_service.repository.IUserCredentialsRepository;
import com.dev.quikkkk.auth_service.service.IAuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final IUserCredentialsRepository userRepository;
    private final IRoleRepository roleRepository;
    private final UserMapper mapper;

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        return null;
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
//        userCredentials.setEnabled(false); // TODO
        userCredentials.setEmailVerified(false);

        log.debug("Saving user: {}", userCredentials);
        userRepository.save(userCredentials);
        log.info("User {} registered", userCredentials.getEmail());

        defaultRole.getUserCredentials().add(userCredentials);
        roleRepository.save(defaultRole);
    }

    private void checkUserEmail(String email) {
        boolean emailExists = userRepository.existsByEmailIgnoreCase(email);
//        if (emailExists) throw new BusinessException(EMAIL_ALREADY_EXISTS); // TODO
    }

    private void checkPasswords(String password, String confirmPassword) {
        // TODO
    }
}
