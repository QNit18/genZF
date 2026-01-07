package com.qnit18.main_service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

    public String getUserIdFromToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                log.warn("Authentication or principal is null");
                return null;
            }

            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String userId = jwt.getClaimAsString("sub");
                if (userId == null || userId.isEmpty()) {
                    log.warn("UserId (sub claim) is null or empty in JWT token");
                    return null;
                }
                return userId;
            } else {
                log.warn("Principal is not a JWT token: {}", authentication.getPrincipal().getClass().getName());
                return null;
            }
        } catch (Exception e) {
            log.error("Error extracting userId from JWT token: {}", e.getMessage(), e);
            return null;
        }
    }
}

