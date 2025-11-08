package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.dto.request.CreateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.request.CreateTrainerScheduleRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.response.TrainerResponse;
import com.dev.quikkkk.user_service.dto.response.TrainerScheduleResponse;
import com.dev.quikkkk.user_service.entity.User;
import com.dev.quikkkk.user_service.enums.RoleTypes;
import com.dev.quikkkk.user_service.exception.BusinessException;
import com.dev.quikkkk.user_service.exception.ErrorCode;
import com.dev.quikkkk.user_service.mapper.TrainerMapper;
import com.dev.quikkkk.user_service.mapper.TrainerScheduleMapper;
import com.dev.quikkkk.user_service.repository.ITrainerRepository;
import com.dev.quikkkk.user_service.repository.ITrainerScheduleRepository;
import com.dev.quikkkk.user_service.repository.IUserRepository;
import com.dev.quikkkk.user_service.service.ITrainerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dev.quikkkk.user_service.exception.ErrorCode.INVALID_ROLE_TRAINER;
import static com.dev.quikkkk.user_service.exception.ErrorCode.TRAINER_PROFILE_ALREADY_EXISTS;
import static com.dev.quikkkk.user_service.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerServiceImpl implements ITrainerService {
    private final ITrainerRepository trainerRepository;
    private final ITrainerScheduleRepository trainerScheduleRepository;
    private final IUserRepository userRepository;
    private final TrainerMapper trainerMapper;
    private final TrainerScheduleMapper trainerScheduleMapper;

    @Override
    @Transactional
    public TrainerResponse createTrainerProfile(String userId, CreateTrainerProfileRequest request) {
        log.info("Creating trainer profile for user: {}", userId);
        var user = findByUserId(userId);

        if (user.getRole() != RoleTypes.ROLE_TRAINER) throw new BusinessException(INVALID_ROLE_TRAINER);
        if (trainerRepository.existsByUserId(userId)) throw new BusinessException(TRAINER_PROFILE_ALREADY_EXISTS);

        var trainer = trainerMapper.createProfile(userId, request);
        var savedTrainer = trainerRepository.save(trainer);

        log.info("Trainer profile created successfully for user: {}", userId);
        return trainerMapper.toTrainerResponse(savedTrainer);
    }

    @Override
    @Transactional
    @CacheEvict(value = "trainers", allEntries = true)
    public TrainerResponse updateTrainerProfile(String userId, UpdateTrainerProfileRequest request) {
        log.info("Updating trainer profile for user: {}", userId);

        var trainer = trainerRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        trainerMapper.updateTrainerProfile(trainer, request);
        var updatedTrainer = trainerRepository.save(trainer);

        log.info("Trainer profile updated successfully for user: {}", userId);
        return trainerMapper.toTrainerResponse(updatedTrainer);
    }

    @Override
    public TrainerResponse getTrainerProfile(String trainerId) {
        return null;
    }

    @Override
    public Page<TrainerResponse> getAllTrainers(int page, int size, String specialization) {
        return null;
    }

    @Override
    public List<TrainerScheduleResponse> getTrainerSchedule(String trainerId) {
        return List.of();
    }

    @Override
    public TrainerScheduleResponse addScheduleSlot(String userId, CreateTrainerScheduleRequest request) {
        return null;
    }

    @Override
    public void deleteScheduleSlot(String userId, String scheduleId) {

    }

    @Override
    public void deleteTrainerProfile(String userId) {

    }

    private User findByUserId(String userId) {
        return userRepository.findByIdAndNotDeleted(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}
