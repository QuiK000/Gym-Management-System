package com.dev.quikkkk.user_service.mapper;

import com.dev.quikkkk.user_service.dto.request.CreateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.response.TrainerResponse;
import com.dev.quikkkk.user_service.entity.TrainerProfile;
import org.springframework.stereotype.Service;

@Service
public class TrainerMapper {
    public TrainerResponse toTrainerResponse(TrainerProfile trainer) {
        return TrainerResponse.builder()
                .id(trainer.getId())
                .userId(trainer.getUserId())
                .firstname(trainer.getUser() != null ? trainer.getUser().getFirstName() : null)
                .lastname(trainer.getUser() != null ? trainer.getUser().getLastName() : null)
                .email(trainer.getUser() != null ? trainer.getUser().getEmail() : null)
                .avatarUrl(trainer.getUser() != null ? trainer.getUser().getAvatarUrl() : null)
                .specialization(trainer.getSpecialization())
                .certification(trainer.getCertification())
                .experienceYears(trainer.getExperienceYears())
                .bio(trainer.getBio())
                .hourlyRate(trainer.getHourlyRate())
                .isAvailable(trainer.getIsAvailable())
                .build();
    }

    public void updateTrainerProfile(TrainerProfile trainer, UpdateTrainerProfileRequest request) {
        if (request.getSpecialization() != null) trainer.setSpecialization(request.getSpecialization());
        if (request.getCertification() != null) trainer.setCertification(request.getCertification());
        if (request.getExperienceYears() != null) trainer.setExperienceYears(request.getExperienceYears());
        if (request.getBio() != null) trainer.setBio(request.getBio());
        if (request.getHourlyRate() != null) trainer.setHourlyRate(request.getHourlyRate());
        if (request.getIsAvailable() != null) trainer.setIsAvailable(request.getIsAvailable());
    }

    public TrainerProfile createProfile(String userId, CreateTrainerProfileRequest request) {
        return TrainerProfile.builder()
                .userId(userId)
                .specialization(request.getSpecialization())
                .certification(request.getCertification())
                .experienceYears(request.getExperienceYears())
                .bio(request.getBio())
                .hourlyRate(request.getHourlyRate())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .createdBy(userId)
                .build();
    }
}
