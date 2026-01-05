package com.qnit18.auth_service.configuration;

import com.qnit18.auth_service.constant.PredefinedRole;
import com.qnit18.auth_service.entity.Role;
import com.qnit18.auth_service.entity.User;
import com.qnit18.auth_service.repository.RoleRepository;
import com.qnit18.auth_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static String ADMIN_USER_NAME = "admin";

    @NonFinal
    static String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.datasource",
            name = "driver-class-name",
            havingValue = "org.postgresql.Driver"
    )
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("MySQL detected, initializing admin user if not exists");
        return args -> {
            if (userRepository.existsByUsername(ADMIN_USER_NAME)) {
                log.info("Admin user already exists, skipping initialization");
                return;
            }
            roleRepository.save(Role.builder()
                    .name(PredefinedRole.ROLE_USER)
                    .description("User role")
                    .build());

            Role adminRole = roleRepository.save(Role.builder()
                    .name(PredefinedRole.ROLE_ADMIN)
                    .description("Admin role")
                    .build());

            var roles = new HashSet<Role>();

            roles.add(adminRole);

            userRepository.save(User.builder()
                            .username(ADMIN_USER_NAME)
                            .password(passwordEncoder.encode(ADMIN_PASSWORD))
                            .firstName("Admin")
                            .lastName("User")
                            .roles(roles)
                            .build()
            );
            log.warn("Admin user created with username: {} and password: {}", ADMIN_USER_NAME, ADMIN_PASSWORD);
        };
    }
}
