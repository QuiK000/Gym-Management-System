package com.dev.quikkkk.auth_service.repository;

import com.dev.quikkkk.auth_service.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserCredentialsRepository extends JpaRepository<UserCredentials, String> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<UserCredentials> findByEmailIgnoreCase(String email);
}
