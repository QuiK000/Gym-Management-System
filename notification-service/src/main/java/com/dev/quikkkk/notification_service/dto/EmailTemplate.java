package com.dev.quikkkk.notification_service.dto;

import lombok.Getter;

public enum EmailTemplate {
    CODE_CONFIRMATION("code-confirmation.html", "Code to confirm email");

    @Getter
    private final String template;

    @Getter
    private final String subject;

    EmailTemplate(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}
