package com.qnit18.main_service.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AuthServiceHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final CircuitBreaker authServiceCircuitBreaker;

    @Value("${services.auth.url}")
    private String authServiceUrl;

    @Override
    public Health health() {
        try {
            restTemplate.getForObject(authServiceUrl + "/actuator/health", String.class);
            return Health.up()
                    .withDetail("auth-service", "UP")
                    .withDetail("circuit-breaker", authServiceCircuitBreaker.getState().name())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("auth-service", "DOWN")
                    .withDetail("circuit-breaker", authServiceCircuitBreaker.getState().name())
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

