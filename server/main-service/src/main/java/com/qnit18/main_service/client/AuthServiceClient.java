package com.qnit18.main_service.client;

import com.qnit18.main_service.dto.response.AuthUserResponse;
import com.qnit18.main_service.exception.AppException;
import com.qnit18.main_service.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthServiceClient {
    
    @Value("${auth-service.url:http://localhost:8080}")
    String authServiceUrl;
    
    final RestTemplate restTemplate;

    public AuthServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AuthUserResponse getUserById(String userId) {
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

