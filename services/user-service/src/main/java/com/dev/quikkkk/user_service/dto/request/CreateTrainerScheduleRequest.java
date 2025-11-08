package com.dev.quikkkk.user_service.dto.request;

import com.dev.quikkkk.user_service.enums.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTrainerScheduleRequest {
    @NotNull(message = "VALIDATION.CREATE.TRAINER.SCHEDULE.REQUEST.DAY_OF_WEEK.NOT_NULL")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "VALIDATION.CREATE.TRAINER.SCHEDULE.REQUEST.START_TIME.NOT_NULL")
    private String startTime;

    @NotNull(message = "VALIDATION.CREATE.TRAINER.SCHEDULE.REQUEST.END_TIME.NOT_NULL")
    private String endTime;
}
