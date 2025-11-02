package com.dev.quikkkk.auth_service.repository;

import com.dev.quikkkk.auth_service.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IEmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    Optional<EmailVerification> findByEmailAndCodeAndVerifiedFalse(String email, String code);

    Optional<EmailVerification> findByUserIdAndVerifiedFalse(String userId);

    Optional<EmailVerification> findByEmailAndVerifiedFalse(String email);

    List<EmailVerification> findByEmailAndCreatedDateAfter(String email, LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.expiresAt < :now")
    void deleteExpiredVerifications(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM EmailVerification e WHERE e.userId = :userId")
    void deleteByUserId(String userId);

    boolean existsByEmailAndVerifiedTrueAndCreatedDateAfter(String email, LocalDateTime dateTime);
}
