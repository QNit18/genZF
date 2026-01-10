package com.qnit18.api_gateway.client;

import com.qnit18.api_gateway.dto.IntrospectRequest;
import com.qnit18.api_gateway.dto.IntrospectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    public Mono<IntrospectResponse> introspectToken(String token) {
        IntrospectRequest request = IntrospectRequest.builder().token(token).build();
        return webClientBuilder.build()
            .post()
            .uri(authServiceUrl + "/auth-service/auth/introspect")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(IntrospectResponse.class)
            .doOnError(error -> log.error("Error calling introspect endpoint: {}", error.getMessage()))
            .onErrorReturn(IntrospectResponse.builder().valid(false).build());
    }
}
