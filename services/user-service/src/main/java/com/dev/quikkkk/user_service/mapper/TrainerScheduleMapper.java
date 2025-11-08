package com.dev.quikkkk.user_service.mapper;

import com.dev.quikkkk.user_service.dto.request.CreateTrainerScheduleRequest;
import com.dev.quikkkk.user_service.dto.response.TrainerScheduleResponse;
import com.dev.quikkkk.user_service.entity.TrainerSchedule;
import org.springframework.stereotype.Service;

@Service
public class TrainerScheduleMapper {
    public TrainerScheduleResponse toScheduleResponse(TrainerSchedule schedule) {
        return TrainerScheduleResponse.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .isActive(schedule.isActive())
                .build();
    }

    public TrainerSchedule toScheduleEntity(String trainerId, CreateTrainerScheduleRequest request) {
        return TrainerSchedule.builder()
                .trainerId(trainerId)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isActive(true)
                .createdBy("SYSTEM")
                .build();
    }
}
