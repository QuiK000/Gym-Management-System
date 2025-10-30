package com.dev.quikkkk.user_service.kafka;

import com.dev.quikkkk.user_service.dto.kafka.UserRegisteredEvent;
import com.dev.quikkkk.user_service.entity.User;
import com.dev.quikkkk.user_service.repository.IUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {
    private final IUserRepository repository;

    @KafkaListener(
            topics = "user-registered-topic",
            groupId = "user-service",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Received user registration event for USER_ID: {}, email: {}", event.getUserId(), event.getEmail());

        try {
            if (repository.existsById(event.getUserId())) {
                log.warn("User profile already exists for USER_ID: {}", event.getUserId());
                return;
            }

            User profile = User.builder()
                    .id(event.getUserId())
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .email(event.getEmail())
                    .role(event.getRole())
                    .phone(null)
                    .dateOfBirth(null)
                    .gender(null)
                    .avatarUrl(null)
                    .address(null)
                    .emergencyContactName(null)
                    .emergencyContactPhone(null)
                    .createdBy("SYSTEM")
                    .createdDate(event.getTimestamp())
                    .build();

            repository.saveAndFlush(profile);
            log.info("User profile created successfully for USER_ID: {}", profile.getId());
        } catch (Exception e) {
            log.error("Failed to create user profile for USER_ID: {}", event.getUserId(), e);
            throw e;
        }
    }
}
