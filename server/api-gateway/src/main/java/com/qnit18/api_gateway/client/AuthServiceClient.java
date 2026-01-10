package com.qnit18.api_gateway.client;

import com.qnit18.api_gateway.dto.ApiBaseResponse;
import com.qnit18.api_gateway.dto.IntrospectRequest;
import com.qnit18.api_gateway.dto.IntrospectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
            .uri(authServiceUrl + "/auth/introspect")
            .header("Content-Type", "application/json")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<ApiBaseResponse<IntrospectResponse>>() {})
            .map(response -> {
                IntrospectResponse result = response.getResult();
                if (result == null) {
                    log.warn("Introspect response result is null");
                    return IntrospectResponse.builder().valid(false).build();
                }
                return result;
            })
            .doOnNext(response -> log.debug("Introspect response: {}", response))
            .doOnError(error -> log.error("Error calling introspect endpoint: {}", error.getMessage()))
            .onErrorReturn(IntrospectResponse.builder().valid(false).build());
    }
}
