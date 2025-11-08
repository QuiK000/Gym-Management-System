package com.dev.quikkkk.user_service.dto.response;

import com.dev.quikkkk.user_service.enums.GenderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainerResponse implements Serializable {
    private String id;
    private String userId;
    private String firstname;
    private String lastname;
    private String email;
    private String avatarUrl;
    private List<String> specialization;
    private String certification;
    private Integer experienceYears;
    private GenderType bio;
    private BigDecimal hourlyRate;
    private boolean isAvailable;
}
