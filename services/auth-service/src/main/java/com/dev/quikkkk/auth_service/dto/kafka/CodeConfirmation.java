package com.dev.quikkkk.auth_service.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CodeConfirmation {
    private String code;
    private String email;
    private String userId;
    private String templateType;
    private Map<String, Object> templateVariables;

    public CodeConfirmation(String userId, String email, String code) {
        this.userId = userId;
        this.email = email;
        this.code = code;
        this.templateType = "EMAIL_VERIFICATION";
    }
}
