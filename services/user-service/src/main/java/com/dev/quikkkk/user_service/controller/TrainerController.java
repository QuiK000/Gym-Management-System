package com.dev.quikkkk.user_service.controller;

import com.dev.quikkkk.user_service.dto.request.CreateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.request.CreateTrainerScheduleRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateTrainerProfileRequest;
import com.dev.quikkkk.user_service.dto.response.ApiResponse;
import com.dev.quikkkk.user_service.dto.response.TrainerResponse;
import com.dev.quikkkk.user_service.dto.response.TrainerScheduleResponse;
import com.dev.quikkkk.user_service.security.UserPrincipal;
import com.dev.quikkkk.user_service.service.ITrainerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainer")
@RequiredArgsConstructor
public class TrainerController {
    private final ITrainerService service;

    @PostMapping("/profile")
    @PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<ApiResponse<TrainerResponse>> createTrainerProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTrainerProfileRequest request
    ) {
        TrainerResponse response = service.createTrainerProfile(principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<ApiResponse<TrainerResponse>> updateTrainerProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateTrainerProfileRequest request
    ) {
        TrainerResponse response = service.updateTrainerProfile(principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TrainerResponse>>> getAllTrainers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String specialization
    ) {
        Page<TrainerResponse> trainers = service.getAllTrainers(page, size, specialization);
        return ResponseEntity.ok(ApiResponse.success(trainers));
    }

    @GetMapping("/{trainer-id}")
    public ResponseEntity<ApiResponse<TrainerResponse>> getTrainerById(
            @PathVariable("trainer-id") String trainerId
    ) {
        TrainerResponse trainer = service.getTrainerProfile(trainerId);
        return ResponseEntity.ok(ApiResponse.success(trainer));
    }

    @GetMapping("/{trainer-id}/schedule")
    public ResponseEntity<ApiResponse<List<TrainerScheduleResponse>>> getTrainerSchedule(
            @PathVariable("trainer-id") String trainerId
    ) {
        List<TrainerScheduleResponse> schedule = service.getTrainerSchedule(trainerId);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<ApiResponse<TrainerScheduleResponse>> addScheduleSlot(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTrainerScheduleRequest request
    ) {
        TrainerScheduleResponse schedule = service.addScheduleSlot(principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    @DeleteMapping("/schedule/{schedule-id}")
    @PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<ApiResponse<Void>> deleteScheduleSlot(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("schedule-id") String scheduleId
    ) {
        service.deleteScheduleSlot(principal.id(), scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('ROLE_TRAINER')")
    public ResponseEntity<ApiResponse<Void>> deleteTrainerProfile(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        service.deleteTrainerProfile(principal.id());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
