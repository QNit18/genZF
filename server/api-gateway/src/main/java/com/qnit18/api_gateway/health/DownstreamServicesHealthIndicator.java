package com.qnit18.api_gateway.health;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DownstreamServicesHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.auth.url}")
    private String authServiceUrl;

    @Value("${services.main.url}")
    private String mainServiceUrl;

    @Override
    public Health health() {
        Map<String, String> services = new HashMap<>();
        boolean allHealthy = true;

        // Check auth-service
        try {
            restTemplate.getForObject(authServiceUrl + "/actuator/health", String.class);
            services.put("auth-service", "UP");
        } catch (Exception e) {
            services.put("auth-service", "DOWN");
            allHealthy = false;
        }

        // Check main-service
        try {
            restTemplate.getForObject(mainServiceUrl + "/actuator/health", String.class);
            services.put("main-service", "UP");
        } catch (Exception e) {
            services.put("main-service", "DOWN");
            allHealthy = false;
        }

        if (allHealthy) {
            return Health.up().withDetails(services).build();
        } else {
            return Health.down().withDetails(services).build();
        }
    }
}

