# Why Calling Introspect Twice Causes Error

## The Actual Problem

Let me show you the **REAL** issue, not a circular dependency loop.

---

## Scenario: What Actually Happens

### When You Call: `GET /users/my-info` (Protected Endpoint)

#### Step 1: Spring Security Needs to Validate JWT
```
Request: GET /users/my-info
Header: Authorization: Bearer TOKEN

Spring Security thinks:
├─ "This is a protected endpoint"
├─ "I need to validate the JWT token"
└─ "Let me call CustomJwtDecoder.decode(token)"
```

#### Step 2: CustomJwtDecoder.decode() is Called (OLD CODE)

```java
// OLD CODE
@Override
public Jwt decode(String token) throws JwtException {
    try {
        // ⚠️ PROBLEM: Calls introspect() from inside decoder
        var response = authenticationService.introspect(
            IntrospectRequest.builder().token(token).build()
        );
        
        if (!response.isValid()) {
            throw new JwtException("Invalid token");
        }
    } catch (Exception e) {
        throw new JwtException("Invalid token");  // ❌ ERROR THROWN HERE
    }
    
    // ... rest of code
}
```

#### Step 3: authenticationService.introspect() is Called

```java
// AuthenticationService.introspect()
public IntrospectResponse introspect(IntrospectRequest request) {
    try {
        verifyToken(request.getToken(), false);  // ← Calls verifyToken()
        return IntrospectResponse.builder().valid(true).build();
    } catch (Exception e) {
        return IntrospectResponse.builder().valid(false).build();  // ❌ Returns false
    }
}
```

#### Step 4: verifyToken() is Called

```java
// AuthenticationService.verifyToken()
SignedJWT verifyToken(String token, boolean isRefresh) throws Exception {
    try {
        // Uses OLD key encoding (SIGNING_KEY.getBytes())
        JWSVerifier verifier = new MACVerifier(SIGNING_KEY);  // ⚠️ WRONG KEY!
        SignedJWT signedJWT = SignedJWT.parse(token);
        
        // Verify signature
        if (!signedJWT.verify(verifier)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);  // ❌ FAILS HERE!
        }
        
        // ... rest of validation
    }
}
```

---

## The REAL Problem: Key Encoding Mismatch

### What Actually Happens:

1. **Token was generated with:**
   ```java
   // Token generation (OLD CODE)
   jwsObject.sign(new MACSigner(SIGNING_KEY));  
   // SIGNING_KEY = Base64 string like "fbX2a4nQ4tdMnfExFUl+uA9aD9IFS+csS8GP96pR75RxrCiUcEYvpn+b4wWsgJshvXMUQiDUxhEBxA9RdPj+OQ=="
   // But MACSigner might treat it as raw bytes or Base64, depending on implementation
   ```

2. **verifyToken() tries to verify with:**
   ```java
   // OLD CODE
   JWSVerifier verifier = new MACVerifier(SIGNING_KEY);
   // Uses SIGNING_KEY directly - might not decode Base64 correctly
   ```

3. **Result:**
   - Signature verification **FAILS** ❌
   - `verifyToken()` throws exception
   - `introspect()` catches exception, returns `valid=false`
   - `CustomJwtDecoder.decode()` sees `valid=false`, throws `JwtException`
   - Spring Security rejects request: **401 Unauthorized**

---

## Why "Twice" is a Problem

### The Issue is NOT a Loop - It's Redundant Validation

**What happens:**

1. **First validation attempt:**
   ```
   CustomJwtDecoder.decode()
   └─ Calls authenticationService.introspect()
      └─ Calls verifyToken()
         └─ Tries to verify signature
            └─ ❌ FAILS (key mismatch)
   ```

2. **Even if first validation passed, decoder would try again:**
   ```java
   // After introspect() check
   return nimbusJwtDecoder.decode(token);  // ← Second validation attempt
   ```

3. **Problem:**
   - We're validating the token **TWICE** (introspect + decoder)
   - Both use potentially **different key encodings**
   - If first fails, we never get to second
   - If first passes but second fails, we still get error

---

## The Real Error Flow

```
1. Request: GET /users/my-info
   ↓
2. Spring Security: "Need to validate JWT"
   ↓
3. CustomJwtDecoder.decode(token) called
   ↓
4. OLD CODE: Calls authenticationService.introspect(token)
   ↓
5. introspect() calls verifyToken(token)
   ↓
6. verifyToken() uses MACVerifier(SIGNING_KEY)
   ├─ Key encoding: SIGNING_KEY.getBytes() or raw string
   └─ Signature verification
      └─ ❌ FAILS (key doesn't match token signature)
   ↓
7. verifyToken() throws exception
   ↓
8. introspect() catches exception, returns valid=false
   ↓
9. CustomJwtDecoder.decode() sees valid=false
   ↓
10. Throws JwtException("Invalid token")
    ↓
11. Spring Security: "Authentication failed"
    ↓
12. Returns 401 Unauthorized ❌
```

---

## Why It's Called "Twice"

### Actually, it's more like "redundant validation":

1. **First validation:** `introspect()` → `verifyToken()` (business logic layer)
2. **Second validation:** `nimbusJwtDecoder.decode()` (cryptographic layer)

**Both try to validate the same token, but:**
- Use potentially different key encodings
- Do duplicate work
- If first fails, second never runs
- Architecture violation (decoder calling service)

---

## The Fix

### NEW CODE: CustomJwtDecoder.decode()

```java
@Override
public Jwt decode(String token) throws JwtException {
    // ✅ Only does cryptographic validation
    // ✅ No service calls
    // ✅ Uses correct Base64-decoded key
    
    if (Objects.isNull(nimbusJwtDecoder)) {
        byte[] keyBytes = Base64.getDecoder().decode(SIGNING_KEY);  // ✅ Correct
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
        nimbusJwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
    
    return nimbusJwtDecoder.decode(token);  // ✅ Simple, fast, correct
}
```

**Now:**
- ✅ Only validates once (in decoder)
- ✅ Uses correct Base64-decoded key
- ✅ No service calls from decoder
- ✅ Fast and simple

**Revocation check happens separately:**
- Gateway calls `/auth/introspect` endpoint (separate HTTP call)
- `AuthenticationService.introspect()` checks database
- This is correct architecture

---

## Summary

**The error is NOT from a circular loop. The error is:**

1. **Key encoding mismatch** - verifyToken() uses wrong key format
2. **Redundant validation** - Validating token twice with different methods
3. **Architecture violation** - Decoder calling service (mixing concerns)
4. **First validation fails** - So decoder throws exception
5. **Result: 401 Unauthorized**

**The fix:**
- Decoder only validates signature/expiry (fast, correct)
- Service handles business logic separately (revocation check)
- Both use same Base64-decoded key
- No redundant validation

