package com.dev.quikkkk.user_service.service;

import com.dev.quikkkk.user_service.dto.request.CreateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.request.CreateTrainerScheduleRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.response.TrainerResponse;
import com.dev.quikkkk.user_service.dto.response.TrainerScheduleResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ITrainerService {
    TrainerResponse createTrainerProfile(String userId, CreateTrainerProfileRequest request);

    TrainerResponse updateTrainerProfile(String userId, UpdateTrainerProfileRequest request);

    TrainerResponse getTrainerProfile(String trainerId);

    Page<TrainerResponse> getAllTrainers(int page, int size, String specialization);

    List<TrainerScheduleResponse> getTrainerSchedule(String trainerId);

    TrainerScheduleResponse addScheduleSlot(String userId, CreateTrainerScheduleRequest request);

    void deleteScheduleSlot(String userId, String scheduleId);

    void deleteTrainerProfile(String userId);
}
