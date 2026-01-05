package com.qnit18.auth_service.controller;

import com.qnit18.auth_service.dto.request.AuthenticationRequest;
import com.qnit18.auth_service.dto.request.IntrospectRequest;
import com.qnit18.auth_service.dto.request.LogoutRequest;
import com.qnit18.auth_service.dto.response.ApiBaseResponse;
import com.qnit18.auth_service.dto.response.AuthenticationResponse;
import com.qnit18.auth_service.dto.response.IntrospectResponse;
import com.qnit18.auth_service.service.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    public ApiBaseResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        var result = authenticationService.authenticate(authenticationRequest);
        return ApiBaseResponse.<AuthenticationResponse>builder()
                .result(AuthenticationResponse.builder()
                        .authenticated(result.isAuthenticated())
                        .token(result.getToken())
                        .build())
                .build();
    }

    @PostMapping("/introspect")
    ApiBaseResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest introspectRequest) {
        var result = authenticationService.introspect(introspectRequest);
        return ApiBaseResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiBaseResponse<Void> logout(@RequestBody LogoutRequest logoutRequest) throws Exception {
        authenticationService.logout(logoutRequest.getToken());
        return ApiBaseResponse.<Void>builder().build();
    }
}
