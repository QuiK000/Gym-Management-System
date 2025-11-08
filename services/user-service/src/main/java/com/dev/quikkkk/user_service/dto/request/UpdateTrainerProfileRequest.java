package com.dev.quikkkk.user_service.dto.request;

import com.dev.quikkkk.user_service.enums.GenderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTrainerProfileRequest {
    private List<String> specialization;

    @Size(max = 500, message = "VALIDATION.UPDATE.TRAINER.PROFILE.CERTIFICATION.SIZE")
    private String certification;

    @Min(value = 0, message = "VALIDATION.UPDATE.TRAINER.PROFILE.EXPERIENCE_YEARS.MIN")
    @Max(value = 50, message = "VALIDATION.UPDATE.TRAINER.PROFILE.EXPERIENCE_YEARS.MAX")
    private Integer experienceYears;
    private GenderType bio;

    @DecimalMin(value = "0.0", message = "VALIDATION.UPDATE.TRAINER.PROFILE.HOURLY_RATE.MIN")
    private BigDecimal hourlyRate;
    private Boolean isAvailable;
}
