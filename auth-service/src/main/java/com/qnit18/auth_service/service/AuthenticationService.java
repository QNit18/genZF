package com.qnit18.auth_service.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.qnit18.auth_service.dto.request.AuthenticationRequest;
import com.qnit18.auth_service.dto.request.IntrospectRequest;
import com.qnit18.auth_service.dto.response.AuthenticationResponse;
import com.qnit18.auth_service.dto.response.IntrospectResponse;
import com.qnit18.auth_service.entity.InvalidedToken;
import com.qnit18.auth_service.entity.User;
import com.qnit18.auth_service.exception.AppException;
import com.qnit18.auth_service.exception.ErrorCode;
import com.qnit18.auth_service.repository.UserRepository;
import com.qnit18.auth_service.repository.ValideTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    @NonFinal
    @Value("${security.signing-key}")
    String SIGNING_KEY;

    UserRepository userRepository;
    ValideTokenRepository valideTokenRepository;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        var userOptional = userRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(),
                userOptional.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(userOptional);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }

    public void logout(String token) throws Exception {
        try {
            var signToken = verifyToken(token);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiredTime = signToken.getJWTClaimsSet().getExpirationTime();
            valideTokenRepository.save(InvalidedToken.builder()
                    .id(jit)
                    .expiredTime(expiredTime)
                    .build());
        } catch (Exception e) {
            log.error("Cannot logout token : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            verifyToken(request.getToken());
            return IntrospectResponse.builder()
                    .valid(true)
                    .build();
        } catch (Exception e) {
            log.info("Token introspection failed: {}", e.getMessage());
            return IntrospectResponse.builder()
                    .valid(false)
                    .build();
        }
    }

    SignedJWT verifyToken(String token) throws Exception {
        try {
            JWSVerifier verifier = new MACVerifier(SIGNING_KEY);
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // Verify signature and expiration
            if (!(signedJWT.verify(verifier) && claims.getExpirationTime().after(new Date()))) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            // Check if token has been invalidated (logged out)
            if (valideTokenRepository.existsById(claims.getJWTID())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return signedJWT;
        } catch (Exception e) {
            log.error("Token verification failed: {}", e.getMessage());
            throw e;
        }
    }

    String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // Token generation logic to be implemented
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("qnit18.com")
                .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNING_KEY));
        } catch (JOSEException e) {
            log.info("Error signing the token: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return jwsObject.serialize();
    }

    String buildScope(User user) {
        StringJoiner scopeJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                scopeJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> scopeJoiner.add(permission.getName()));
                }
            });
            return scopeJoiner.toString();
        } else {
            return "";
        }
    }
}
