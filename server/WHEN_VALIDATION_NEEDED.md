# When Does CustomJwtDecoder.decode() Get Called?

## Quick Answer

**CustomJwtDecoder.decode() is called ONLY for PROTECTED endpoints that require authentication.**

---

## Detailed Explanation

### 1. Public Endpoints (NO JWT Validation)

Looking at `SecurityConfig.java`:

```java
private final String[] PUBLIC_ENDPOINTS = {
    "/users",              // POST /users (register)
    "/auth/token",         // POST /auth/token (login)
    "/auth/introspect",    // POST /auth/introspect (check token)
    "/auth/logout",        // POST /auth/logout
    "/auth/refresh-token"  // POST /auth/refresh-token
};

httpSecurity.authorizeHttpRequests(request ->
    request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
            .anyRequest().authenticated());
```

**For these endpoints:**
- ✅ **NO** JWT token required
- ✅ **NO** CustomJwtDecoder.decode() called
- ✅ Request goes directly to controller

**Example:**
```
POST /auth/introspect
├─ Public endpoint ✅
├─ CustomJwtDecoder.decode() NOT called
└─ Goes directly to AuthenticationController.introspect()
```

---

### 2. Protected Endpoints (JWT Validation REQUIRED)

**For ALL other endpoints:**
- ❌ JWT token **REQUIRED**
- ❌ CustomJwtDecoder.decode() **IS CALLED**
- ❌ Must have valid JWT in Authorization header

**Examples of Protected Endpoints:**

#### Example 1: GET /users/my-info
```java
@GetMapping("/my-info")
@PreAuthorize("isAuthenticated()")  // ← Requires authentication
ApiBaseResponse<UserResponse> getMyInfo() {
    return ApiBaseResponse.<UserResponse>builder()
            .result(userService.getMyInfo())
            .build();
}
```

**Flow:**
```
1. Request: GET /users/my-info
   Header: Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

2. Spring Security checks:
   ├─ Is this a public endpoint? NO
   └─ Requires authentication? YES

3. Spring Security extracts JWT token

4. Spring Security calls: CustomJwtDecoder.decode(token) ⚠️
   └─ This is where validation happens!

5. If decode() succeeds:
   ├─ Creates Authentication object
   └─ Allows request to proceed

6. If decode() fails:
   └─ Returns 401 Unauthorized
```

#### Example 2: GET /users/{userId}
```java
@GetMapping("/{userId}")
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")  // ← Requires authentication
UserResponse getUser(@PathVariable("userId") String userId){
    return userService.getUser(userId);
}
```

**Flow:**
```
1. Request: GET /users/123
   Header: Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

2. Spring Security calls: CustomJwtDecoder.decode(token) ⚠️

3. If valid:
   ├─ Checks @PreAuthorize: hasRole('ADMIN') or hasRole('USER')
   └─ If user has role, allows request
```

---

## The Problem with OLD Code

### Scenario: Calling Protected Endpoint

```
Request: GET /users/my-info
Header: Authorization: Bearer TOKEN
```

**Step-by-step with OLD code:**

1. **Spring Security receives request**
   ```
   ├─ Endpoint: /users/my-info
   ├─ Is public? NO
   └─ Requires authentication? YES
   ```

2. **Spring Security calls CustomJwtDecoder.decode(token)**
   ```java
   // OLD CODE in CustomJwtDecoder.decode()
   try {
       var response = authenticationService.introspect(...);  // ⚠️ PROBLEM!
       if (!response.isValid()) {
           throw new JwtException("Invalid token");
       }
   } catch (Exception e) {
       throw new JwtException("Invalid token");
   }
   ```

3. **What's wrong here?**
   - ❌ Decoder calls `authenticationService.introspect()`
   - ❌ `introspect()` calls `verifyToken()` which:
     - Validates signature (decoder should do this)
     - Checks expiration (decoder should do this)
     - Checks database for revoked tokens (business logic - decoder shouldn't do this)
   - ❌ Mixing concerns: Decoder doing business logic
   - ❌ Slower: Database lookup in decoder
   - ❌ Potential loops if verifyToken() somehow triggers decoder again

---

## The Solution with NEW Code

### Same Scenario: Calling Protected Endpoint

```
Request: GET /users/my-info
Header: Authorization: Bearer TOKEN
```

**Step-by-step with NEW code:**

1. **Spring Security receives request**
   ```
   ├─ Endpoint: /users/my-info
   ├─ Is public? NO
   └─ Requires authentication? YES
   ```

2. **Spring Security calls CustomJwtDecoder.decode(token)**
   ```java
   // NEW CODE in CustomJwtDecoder.decode()
   if (Objects.isNull(nimbusJwtDecoder)) {
       byte[] keyBytes = Base64.getDecoder().decode(SIGNING_KEY);
       SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
       nimbusJwtDecoder = NimbusJwtDecoder
               .withSecretKey(secretKey)
               .macAlgorithm(MacAlgorithm.HS512)
               .build();
   }
   
   return nimbusJwtDecoder.decode(token);  // ✅ Simple, fast, no service calls
   ```

3. **What's right here?**
   - ✅ Decoder ONLY validates signature and expiry
   - ✅ No service calls
   - ✅ No database lookups
   - ✅ Fast and simple
   - ✅ No circular dependencies

4. **Revocation check happens separately:**
   - Gateway calls `/auth/introspect` endpoint (separate HTTP call)
   - `AuthenticationService.introspect()` checks database
   - This is business logic, not decoder responsibility

---

## Summary Table

| Endpoint Type | JWT Required? | CustomJwtDecoder.decode() Called? | Example |
|--------------|----------------|----------------------------------|---------|
| **Public** | ❌ NO | ❌ NO | POST /auth/introspect |
| **Protected** | ✅ YES | ✅ YES | GET /users/my-info |

---

## Key Takeaway

**CustomJwtDecoder.decode() is called by Spring Security when:**
1. Request comes to a **protected endpoint**
2. Request has an **Authorization header with Bearer token**
3. Spring Security needs to **validate the JWT** before allowing access

**The decoder should ONLY:**
- ✅ Validate JWT signature
- ✅ Check if token is expired
- ✅ Return decoded JWT object

**The decoder should NOT:**
- ❌ Call business logic services
- ❌ Check database for revoked tokens
- ❌ Do anything beyond cryptographic validation

