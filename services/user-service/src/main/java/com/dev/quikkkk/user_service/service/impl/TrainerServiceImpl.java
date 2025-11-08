package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.dto.request.CreateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.request.CreateTrainerScheduleRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.response.TrainerResponse;
import com.dev.quikkkk.user_service.dto.response.TrainerScheduleResponse;
import com.dev.quikkkk.user_service.mapper.TrainerMapper;
import com.dev.quikkkk.user_service.mapper.TrainerScheduleMapper;
import com.dev.quikkkk.user_service.repository.ITrainerRepository;
import com.dev.quikkkk.user_service.repository.ITrainerScheduleRepository;
import com.dev.quikkkk.user_service.repository.IUserRepository;
import com.dev.quikkkk.user_service.service.ITrainerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return null;
    }

    @Override
    public TrainerResponse updateTrainerProfile(String userId, UpdateTrainerProfileRequest request) {
        return null;
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
}
