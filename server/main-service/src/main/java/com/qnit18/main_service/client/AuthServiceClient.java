package com.qnit18.main_service.client;

import com.qnit18.main_service.dto.response.AuthUserResponse;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthServiceClient {
    
    @Value("${services.auth.url:http://localhost:8081}")
    String authServiceUrl;
    
    final RestTemplate restTemplate;
    final CircuitBreaker circuitBreaker;
    final Retry retry;

    public AuthServiceClient(RestTemplate restTemplate, CircuitBreaker authServiceCircuitBreaker, Retry authServiceRetry) {
        this.restTemplate = restTemplate;
        this.circuitBreaker = authServiceCircuitBreaker;
        this.retry = authServiceRetry;
    }

    public AuthUserResponse getUserById(String userId) {
        Supplier<AuthUserResponse> supplier = () -> fetchUserById(userId);
        
        // Apply circuit breaker and retry patterns
        supplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        supplier = Retry.decorateSupplier(retry, supplier);
        
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Failed to fetch user after retries and circuit breaker: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private AuthUserResponse fetchUserById(String userId) {
        try {
            log.debug("Fetching user from auth-service: {}", userId);
            String url = authServiceUrl + "/users/{userId}";
            AuthUserResponse user = restTemplate.getForObject(url, AuthUserResponse.class, userId);
            
            if (user == null) {
                log.warn("User not found in auth-service: {}", userId);
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
            
            return user;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("User not found in auth-service: {}", userId);
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error calling auth-service: {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        } catch (Exception e) {
            log.error("Unexpected error calling auth-service: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public boolean validateUser(String userId) {
        try {
            getUserById(userId);
            return true;
        } catch (AppException e) {
            if (e.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
                return false;
            }
            throw e;
        }
    }
}

