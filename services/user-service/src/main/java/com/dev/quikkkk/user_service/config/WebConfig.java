package com.dev.quikkkk.user_service.config;

import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PagedResourcesAssembler;

@Configuration
public class WebConfig {
    @Bean
    public PagedResourcesAssembler<UserProfileResponse> pagedResourcesAssembler() {
        return new PagedResourcesAssembler<>(null, null);
    }
}
