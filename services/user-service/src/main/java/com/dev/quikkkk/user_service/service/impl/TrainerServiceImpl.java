package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.dto.request.CreateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.request.CreateTrainerScheduleRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.response.TrainerResponse;
import com.dev.quikkkk.user_service.dto.response.TrainerScheduleResponse;
import com.dev.quikkkk.user_service.entity.TrainerProfile;
import com.dev.quikkkk.user_service.entity.TrainerSchedule;
import com.dev.quikkkk.user_service.entity.User;
import com.dev.quikkkk.user_service.enums.RoleTypes;
import com.dev.quikkkk.user_service.exception.BusinessException;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dev.quikkkk.user_service.exception.ErrorCode.CANNOT_DELETE_OTHER_TRAINERS_SCHEDULE;
import static com.dev.quikkkk.user_service.exception.ErrorCode.INVALID_ROLE_TRAINER;
import static com.dev.quikkkk.user_service.exception.ErrorCode.SCHEDULE_NOT_FOUND;
import static com.dev.quikkkk.user_service.exception.ErrorCode.START_TIME_AFTER_END_TIME;
import static com.dev.quikkkk.user_service.exception.ErrorCode.TRAINER_NOT_FOUND;
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
    private final SchedulingConfigurer schedulingConfigurer;

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

        TrainerProfile trainer = trainerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        trainerMapper.updateTrainerProfile(trainer, request);

        TrainerProfile updatedTrainer = trainerRepository.save(trainer);

        log.info("Trainer profile updated successfully for user: {}", userId);
        return trainerMapper.toTrainerResponse(updatedTrainer);
    }

    @Override
    @Cacheable(value = "trainers", key = "#userId", unless = "#result == null")
    public TrainerResponse getTrainerProfile(String trainerId) {
        log.info("Getting trainer profile: {}", trainerId);
        TrainerProfile trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new BusinessException(TRAINER_NOT_FOUND));

        return trainerMapper.toTrainerResponse(trainer);
    }

    @Override
    @Cacheable(value = "trainers", key = "'list:' + #page + ':' + #size + ':' + #specialization")
    public Page<TrainerResponse> getAllTrainers(int page, int size, String specialization) {
        log.info("Getting all trainers. Page: {}, Size: {}, Specialization: {}", page, size, specialization);

        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("experienceYears").descending());
        Page<TrainerProfile> trainers = trainerRepository.findAvailablyTrainers(specialization, pageable);
        Page<TrainerResponse> response = trainers.map(trainerMapper::toTrainerResponse);

        log.info("Retrieved {} trainers out of {} total", response.getNumberOfElements(), response.getTotalElements());
        return response;
    }

    @Override
    @Cacheable(value = "trainers", key = "'schedule:' + #trainerId")
    public List<TrainerScheduleResponse> getTrainerSchedule(String trainerId) {
        log.info("Getting schedule for trainer: {}", trainerId);
        if (!trainerRepository.existsById(trainerId)) throw new BusinessException(TRAINER_NOT_FOUND);
        List<TrainerSchedule> schedules = trainerScheduleRepository.findByTrainerIdAndIsActiveTrue(trainerId);

        return schedules.stream()
                .map(trainerScheduleMapper::toScheduleResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "trainers", key = "'schedule:' + #result.id")
    public TrainerScheduleResponse addScheduleSlot(String userId, CreateTrainerScheduleRequest request) {
        log.info("Adding schedule slot for user: {}", userId);
        TrainerProfile trainer = trainerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(TRAINER_NOT_FOUND));

        if (request.getStartTime().isAfter(request.getEndTime()))
            throw new BusinessException(START_TIME_AFTER_END_TIME);

        TrainerSchedule schedule = trainerScheduleMapper.toScheduleEntity(trainer.getId(), request);
        TrainerSchedule savedSchedule = trainerScheduleRepository.save(schedule);

        log.info("Schedule slot added successfully for trainer: {}", trainer.getId());
        return trainerScheduleMapper.toScheduleResponse(savedSchedule);
    }

    @Override
    @Transactional
    @CacheEvict(value = "trainers", allEntries = true)
    public void deleteScheduleSlot(String userId, String scheduleId) {
        log.info("Deleting schedule slot: {} for user: {}", scheduleId, userId);
        TrainerProfile trainer = trainerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(TRAINER_NOT_FOUND));
        TrainerSchedule schedule = trainerScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(SCHEDULE_NOT_FOUND));

        if (!schedule.getTrainerId().equals(trainer.getId()))
            throw new BusinessException(CANNOT_DELETE_OTHER_TRAINERS_SCHEDULE);

        trainerScheduleRepository.delete(schedule);
        log.info("Schedule slot deleted successfully for trainer: {}", trainer.getId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "trainers", allEntries = true)
    public void deleteTrainerProfile(String userId) {
        log.info("Deleting trainer profile for user: {}", userId);
        TrainerProfile trainer = trainerRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(TRAINER_NOT_FOUND));

        trainerScheduleRepository.deleteByTrainerId(trainer.getId());
        trainerRepository.deleteById(trainer.getId());

        log.info("Trainer profile deleted successfully for user: {}", userId);
    }

    private User findByUserId(String userId) {
        return userRepository.findByIdAndNotDeleted(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}
