package com.qnit18.api_gateway.security;

import com.qnit18.api_gateway.client.AuthServiceClient;
import com.qnit18.api_gateway.dto.IntrospectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayJwtAuthenticationFilterTest {

    @Mock
    private ReactiveJwtDecoder jwtDecoder;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private WebFilterChain filterChain;

    private GatewayJwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GatewayJwtAuthenticationFilter(jwtDecoder, authServiceClient);
    }

    @Test
    void filter_WithValidToken_ShouldAuthenticate() {
        // Arrange
        String token = "valid.jwt.token";
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", "ROLE_USER");
        claims.put("sub", "testuser");
        
        Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), 
                         Map.of("alg", "HS512"), claims);

        MockServerHttpRequest request = MockServerHttpRequest
            .get("/test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtDecoder.decode(token)).thenReturn(Mono.just(jwt));
        when(authServiceClient.introspectToken(token))
            .thenReturn(Mono.just(IntrospectResponse.builder().valid(true).build()));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, filterChain))
            .verifyComplete();
    }

    @Test
    void filter_WithoutToken_ShouldContinueWithoutAuthentication() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, filterChain))
            .verifyComplete();
    }

    @Test
    void filter_WithInvalidToken_ShouldContinueWithoutAuthentication() {
        // Arrange
        String token = "invalid.jwt.token";
        
        MockServerHttpRequest request = MockServerHttpRequest
            .get("/test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtDecoder.decode(token)).thenReturn(Mono.error(new RuntimeException("Invalid token")));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, filterChain))
            .verifyComplete();
    }

    @Test
    void filter_WithRevokedToken_ShouldNotAuthenticate() {
        // Arrange
        String token = "revoked.jwt.token";
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", "ROLE_USER");
        claims.put("sub", "testuser");
        
        Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), 
                         Map.of("alg", "HS512"), claims);

        MockServerHttpRequest request = MockServerHttpRequest
            .get("/test")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtDecoder.decode(token)).thenReturn(Mono.just(jwt));
        when(authServiceClient.introspectToken(token))
            .thenReturn(Mono.just(IntrospectResponse.builder().valid(false).build()));
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(filter.filter(exchange, filterChain))
            .verifyComplete();
    }
}
