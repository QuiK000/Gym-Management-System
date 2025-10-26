package com.dev.quikkkk.user_service.kafka;

import com.dev.quikkkk.user_service.dto.kafka.UserRegisteredEvent;
import com.dev.quikkkk.user_service.entity.UserProfile;
import com.dev.quikkkk.user_service.repository.IUserProfileRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {
    private final EntityManager entityManager;
    private final IUserProfileRepository repository;

    @KafkaListener(
            topics = "user-registered-topic",
            groupId = "user-service",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Received user registration event for USER_ID: {}, email: {}", event.getUserId(), event.getEmail());
        try {
            UserProfile existing = entityManager.find(UserProfile.class, event.getUserId());
            if (existing != null) {
                log.warn("User profile already exists for USER_ID: {}", event.getUserId());
                return;
            }

            UserProfile profile = UserProfile.builder()
                    .id(event.getUserId())
                    .firstName("User")
                    .lastName(UUID.randomUUID().toString())
                    .email(event.getEmail())
                    .phone(null)
                    .dateOfBirth(null)
                    .gender(null)
                    .avatarUrl(null)
                    .address(null)
                    .emergencyContactName(null)
                    .emergencyContactPhone(null)
                    .createdBy("SYSTEM")
                    .build();

            entityManager.persist(profile);
            entityManager.flush();
            log.info("User profile saved for USER_ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to create user profile for USER_ID: {}", event.getUserId(), e);
            throw e;
        }
    }
}
