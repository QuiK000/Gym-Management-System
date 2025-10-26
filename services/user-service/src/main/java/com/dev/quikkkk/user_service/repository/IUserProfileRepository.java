package com.dev.quikkkk.user_service.repository;

import com.dev.quikkkk.user_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByEmail(String email);

    boolean existsByEmail(String email);
}
