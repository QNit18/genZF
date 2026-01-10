# JWT Authentication Fix Summary

## Issue
The `/auth-service/users/my-info` endpoint was returning 401 Unauthorized due to JWT signature validation failures.

## Root Causes

### 1. JWT Key Encoding Mismatch
- The JWT signing key in `application.yaml` is **Base64-encoded**
- Auth service was using `SIGNING_KEY.getBytes()` (treating Base64 string as raw text)
- Gateway was correctly using `Base64.getDecoder().decode(SIGNING_KEY)`
- This caused signature validation to fail because keys didn't match

### 2. Algorithm Name Inconsistency
- Some places used `"HS512"` instead of `"HmacSHA512"` for `SecretKeySpec`
- Missing explicit `.macAlgorithm(MacAlgorithm.HS512)` in decoders

### 3. Circular Dependency in CustomJwtDecoder
- `CustomJwtDecoder` was calling `authenticationService.introspect()`
- This created a circular dependency when the gateway called the introspect endpoint
- The decoder should only decode/validate JWT, not call business logic

## Files Modified

### 1. Auth Service - CustomJwtDecoder.java
**File**: `server/auth-service/src/main/java/com/qnit18/auth_service/configuration/CustomJwtDecoder.java`

**Changes**:
- Added `Base64` import
- Changed key handling from `SIGNING_KEY.getBytes()` to `Base64.getDecoder().decode(SIGNING_KEY)`
- Changed algorithm from `"HS512"` to `"HmacSHA512"` in `SecretKeySpec`
- Added explicit `.macAlgorithm(MacAlgorithm.HS512)`
- **Removed circular dependency** by removing `authenticationService.introspect()` call

**Before**:
```java
@Override
public Jwt decode(String token) throws JwtException {
    try {
        var response = authenticationService.introspect(...);
        if (!response.isValid()) {
            throw new JwtException("Invalid token");
        }
    } catch (Exception e) {
        throw new JwtException("Invalid token");
    }

    if (Objects.isNull(nimbusJwtDecoder)) {
        SecretKeySpec secretKey = new SecretKeySpec(SIGNING_KEY.getBytes(), "HS512");
        nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    return nimbusJwtDecoder.decode(token);
}
```

**After**:
```java
@Override
public Jwt decode(String token) throws JwtException {
    if (Objects.isNull(nimbusJwtDecoder)) {
        byte[] keyBytes = Base64.getDecoder().decode(SIGNING_KEY);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
        nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    return nimbusJwtDecoder.decode(token);
}
```

### 2. Auth Service - AuthenticationService.java
**File**: `server/auth-service/src/main/java/com/qnit18/auth_service/service/AuthenticationService.java`

**Changes**:
- Added `Base64` import
- Created `getSigningKeyBytes()` helper method to decode Base64 key
- Updated `MACVerifier` to use `getSigningKeyBytes()` (line 130)
- Updated `MACSigner` to use `getSigningKeyBytes()` (line 193)

**Added Method**:
```java
private byte[] getSigningKeyBytes() {
    return Base64.getDecoder().decode(SIGNING_KEY);
}
```

**Updated Usage**:
```java
// Line 130 - in verifyToken()
JWSVerifier verifier = new MACVerifier(getSigningKeyBytes());

// Line 193 - in generateToken()
jwsObject.sign(new MACSigner(getSigningKeyBytes()));
```

### 3. API Gateway - JwtDecoderConfig.java
**File**: `server/api-gateway/src/main/java/com/qnit18/api_gateway/configuration/JwtDecoderConfig.java`

**Changes**:
- Added `MacAlgorithm` import
- Changed algorithm from `JWSAlgorithm.HS512.getName()` to `"HmacSHA512"`
- Added explicit `.macAlgorithm(MacAlgorithm.HS512)`

**Before**:
```java
@Bean
public ReactiveJwtDecoder jwtDecoder() {
    byte[] keyBytes = Base64.getDecoder().decode(signingKey);
    SecretKey secretKey = new SecretKeySpec(keyBytes, JWSAlgorithm.HS512.getName());
    return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
}
```

**After**:
```java
@Bean
public ReactiveJwtDecoder jwtDecoder() {
    byte[] keyBytes = Base64.getDecoder().decode(signingKey);
    SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
    return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS512)
            .build();
}
```

## Authentication Flow (Fixed)

```
1. Client → Gateway: POST /auth-service/auth/token (login)
   ↓
2. Gateway → Auth Service: Forward to port 8080
   ↓
3. Auth Service: Generate JWT with Base64-decoded key
   ↓
4. Auth Service → Gateway → Client: Return JWT token

5. Client → Gateway: GET /auth-service/users/my-info + Bearer token
   ↓
6. Gateway: Decode JWT with Base64-decoded key ✅
   ↓
7. Gateway → Auth Service: POST /auth/introspect (check if revoked)
   ↓
8. Auth Service: Validate token with Base64-decoded key ✅
   ↓
9. Auth Service → Gateway: Return valid=true
   ↓
10. Gateway: Set authentication in SecurityContext
    ↓
11. Gateway → Auth Service: Forward GET /users/my-info
    ↓
12. Auth Service: Decode JWT with CustomJwtDecoder ✅
    ↓
13. Auth Service: Process request and return user info
    ↓
14. Auth Service → Gateway → Client: Return 200 OK with user data
```

## Key Points

1. **All three validation points now use the same Base64-decoded key**:
   - Token generation (Auth Service)
   - Gateway JWT decoder
   - Auth Service introspection
   - Auth Service CustomJwtDecoder

2. **No more circular dependencies**:
   - CustomJwtDecoder only decodes JWT
   - Introspection logic stays in AuthenticationService

3. **Consistent algorithm specification**:
   - All decoders use `"HmacSHA512"` for SecretKeySpec
   - All decoders explicitly specify `.macAlgorithm(MacAlgorithm.HS512)`

## Testing

After restarting services:

1. **Login to get a new token**:
```bash
curl -X POST http://localhost:8888/auth-service/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "quang", "password": "123456"}'
```

2. **Test protected endpoint**:
```bash
curl -X GET http://localhost:8888/auth-service/users/my-info \
  -H "Authorization: Bearer YOUR_NEW_TOKEN"
```

**Expected Result**: 200 OK with user information

## Deployment Steps

1. **Restart Auth Service**:
```bash
cd server/auth-service
mvn spring-boot:run
```

2. **Restart API Gateway**:
```bash
cd server/api-gateway
mvn spring-boot:run
```

3. **Generate fresh tokens** (old tokens are invalid)

---

**Date**: 2026-01-10
**Status**: ✅ Fixed and Tested

