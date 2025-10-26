package com.dev.quikkkk.auth_service;

import com.dev.quikkkk.auth_service.entity.Role;
import com.dev.quikkkk.auth_service.repository.IRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@Slf4j
@EnableFeignClients
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

    @Bean
    public CommandLineRunner commandLineRunner(IRoleRepository repository) {
        return args -> {
            createRoleIfNotExists(repository, "ROLE_ADMIN");
            createRoleIfNotExists(repository, "ROLE_TRAINER");
            createRoleIfNotExists(repository, "ROLE_MEMBER");
        };
    }

    private void createRoleIfNotExists(IRoleRepository repository, String roleName) {
        Optional<Role> existingRole = repository.findByName(roleName);
        log.info("Checking if role {} exists", roleName);

        if (existingRole.isEmpty()) {
            Role role = Role.builder()
                    .name(roleName)
                    .createdBy("system")
                    .build();

            repository.save(role);
            log.info("Role {} created", roleName);
        }
    }
}
