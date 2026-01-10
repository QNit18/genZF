# Detailed Flow Comparison: Before vs After Code Change

## API Call: `GET /auth-service/users/my-info` with Bearer Token

---

## 🔴 BEFORE THE CHANGE (With Circular Dependency)

### Step-by-Step Flow:

#### **Step 1: Client Makes Request**
```
Client → Gateway (Port 8888)
Request: GET /auth-service/users/my-info
Header: Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

#### **Step 2: Gateway Receives Request**
```
GatewayJwtAuthenticationFilter.filter() is called
├─ Extracts token from Authorization header
└─ Token: "eyJhbGciOiJIUzUxMiJ9..."
```

#### **Step 3: Gateway Decodes JWT Locally**
```
GatewayJwtAuthenticationFilter (Line 45)
├─ jwtDecoder.decode(token) ✅ SUCCESS
└─ JWT decoded successfully
```

#### **Step 4: Gateway Calls Introspect Endpoint**
```
GatewayJwtAuthenticationFilter (Line 48)
├─ authServiceClient.introspectToken(token)
└─ Makes HTTP call: POST http://localhost:8080/auth/introspect
```

#### **Step 5: Auth Service Receives Introspect Request**
```
Auth Service (Port 8080)
├─ Request: POST /auth/introspect
├─ Body: { "token": "eyJhbGciOiJIUzUxMiJ9..." }
└─ Spring Security Filter Chain starts
```

#### **Step 6: Spring Security Processes Request** ✅ NO PROBLEM HERE
```
Spring Security Filter Chain
├─ Checks: Is /auth/introspect a public endpoint?
├─ SecurityConfig.java (Line 31): 
│  └─ "/auth/introspect" is in PUBLIC_ENDPOINTS ✅
├─ SecurityConfig.java (Line 42):
│  └─ requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
└─ Result: Request allowed through WITHOUT JWT validation
   └─ CustomJwtDecoder.decode() is NOT called for public endpoints ✅
```

#### **Step 7: The REAL Problem - When CustomJwtDecoder IS Called** 🔴
```
IMPORTANT: CustomJwtDecoder.decode() is called for PROTECTED endpoints!

Example: When you call GET /users/my-info (protected endpoint)
├─ SecurityConfig.java (Line 43): .anyRequest().authenticated()
├─ Spring Security sees: "This endpoint needs authentication"
├─ Spring Security extracts JWT token from Authorization header
└─ Spring Security calls: CustomJwtDecoder.decode(token) ⚠️

OLD CODE PROBLEM:
CustomJwtDecoder.decode(token) (OLD CODE)
├─ Line 30: var response = authenticationService.introspect(...)
│  └─ ⚠️ PROBLEM: Calls business logic from inside decoder!
│
├─ What's wrong:
│  1. Decoder should ONLY validate signature/expiry (fast, simple)
│  2. But OLD code calls introspect() which:
│     - Calls verifyToken() (business logic)
│     - Checks database for revoked tokens (slow)
│     - Does more than a decoder should do
│  3. This mixes concerns:
│     - Decoder = Cryptographic validation
│     - Service = Business logic validation
│  4. If decoder calls service, and service needs decoder... LOOP! ❌
│
└─ Result: Architecture violation, potential loops, slower performance
```

#### **Step 8: Exception Handling**
```
CustomJwtDecoder.decode() (OLD CODE)
├─ catch (Exception e) {
│  └─ throw new JwtException("Invalid token");
└─ }
└─ JWT validation fails ❌
```

#### **Step 9: Gateway Receives Error**
```
GatewayJwtAuthenticationFilter
├─ onErrorResume() catches error (Line 71)
├─ Logs: "JWT validation failed"
└─ Returns: Request continues WITHOUT authentication
```

#### **Step 10: Request Forwarded to Auth Service**
```
Gateway → Auth Service: GET /users/my-info
├─ NO Authorization header set (because authentication failed)
└─ Auth Service Security rejects it: 401 Unauthorized ❌
```

#### **Step 11: Final Response**
```
Client receives: 401 Unauthorized ❌
```

---

## ✅ AFTER THE CHANGE (Fixed - No Circular Dependency)

### Step-by-Step Flow:

#### **Step 1: Client Makes Request**
```
Client → Gateway (Port 8888)
Request: GET /auth-service/users/my-info
Header: Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```
✅ Same as before

#### **Step 2: Gateway Receives Request**
```
GatewayJwtAuthenticationFilter.filter() is called
├─ Extracts token from Authorization header
└─ Token: "eyJhbGciOiJIUzUxMiJ9..."
```
✅ Same as before

#### **Step 3: Gateway Decodes JWT Locally**
```
GatewayJwtAuthenticationFilter (Line 45)
├─ jwtDecoder.decode(token) ✅ SUCCESS
└─ JWT decoded successfully
```
✅ Same as before

#### **Step 4: Gateway Calls Introspect Endpoint**
```
GatewayJwtAuthenticationFilter (Line 48)
├─ authServiceClient.introspectToken(token)
└─ Makes HTTP call: POST http://localhost:8080/auth/introspect
   └─ Body: { "token": "eyJhbGciOiJIUzUxMiJ9..." }
```
✅ Same as before

#### **Step 5: Auth Service Receives Introspect Request**
```
Auth Service (Port 8080)
├─ Request: POST /auth/introspect
├─ Body: { "token": "eyJhbGciOiJIUzUxMiJ9..." }
└─ Spring Security Filter Chain starts
```
✅ Same as before

#### **Step 6: Spring Security Processes Request** ✅ NO PROBLEM
```
Spring Security Filter Chain
├─ Checks: Is /auth/introspect a public endpoint?
├─ SecurityConfig.java (Line 42):
│  └─ requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
│     └─ /auth/introspect is in PUBLIC_ENDPOINTS ✅
└─ Request allowed through (no JWT validation needed)
```
✅ Public endpoint, no decoder called

#### **Step 7: AuthenticationController.introspect() is Called**
```
AuthenticationController.introspect() (Line 38)
├─ Receives: IntrospectRequest { token: "..." }
└─ Calls: authenticationService.introspect(introspectRequest)
```
✅ Direct service call, no security filter

#### **Step 8: AuthenticationService.introspect() Executes**
```
AuthenticationService.introspect() (Line 95)
├─ Calls: verifyToken(request.getToken(), false)
└─ verifyToken() checks:
   ├─ Signature validation ✅
   ├─ Expiration check ✅
   └─ Revocation check (database lookup) ✅
```
✅ Business logic executes normally

#### **Step 9: verifyToken() Validates Token**
```
AuthenticationService.verifyToken() (Line 125)
├─ Line 130: JWSVerifier verifier = new MACVerifier(getSigningKeyBytes())
│  └─ Uses Base64-decoded key ✅
├─ Line 131: signedJWT.verify(verifier)
│  └─ Signature valid ✅
├─ Line 149: Check expiration
│  └─ Token not expired ✅
└─ Line 155: Check if token revoked
   └─ Token not in revoked list ✅
```
✅ All validations pass

#### **Step 10: Introspect Response Returned**
```
AuthenticationService.introspect() (Line 98)
├─ Returns: IntrospectResponse { valid: true }
└─ AuthenticationController wraps it in ApiBaseResponse
```
✅ Returns valid=true

#### **Step 11: Gateway Receives Introspect Response**
```
GatewayJwtAuthenticationFilter (Line 49)
├─ introspectResponse.isValid() = true ✅
└─ Continues to Step 12
```
✅ Token is valid

#### **Step 12: Gateway Sets Authentication**
```
GatewayJwtAuthenticationFilter (Line 56-68)
├─ Extracts scope from JWT: "ROLE_USER UPDATE_DATA CREATE_DATA"
├─ Creates authorities: [ROLE_USER, UPDATE_DATA, CREATE_DATA]
├─ Creates UsernamePasswordAuthenticationToken
└─ Sets in SecurityContext ✅
```
✅ Authentication established

#### **Step 13: Gateway Forwards Request to Auth Service**
```
Gateway → Auth Service: GET /users/my-info
├─ Authorization header: Bearer eyJhbGciOiJIUzUxMiJ9...
└─ Request forwarded with token
```
✅ Token included

#### **Step 14: Auth Service Receives Request**
```
Auth Service Security Filter
├─ Request: GET /users/my-info
├─ Authorization header present ✅
└─ Spring Security uses CustomJwtDecoder
```
✅ Security filter processes request

#### **Step 15: CustomJwtDecoder.decode() is Called** ✅ NO CIRCULAR DEPENDENCY
```
CustomJwtDecoder.decode(token) (NEW CODE)
├─ Line 25-32: Initialize decoder (if needed)
│  ├─ Base64.decode(SIGNING_KEY) ✅
│  ├─ Create SecretKeySpec with "HmacSHA512" ✅
│  └─ Build NimbusJwtDecoder ✅
│
├─ Line 34: nimbusJwtDecoder.decode(token)
│  └─ Validates signature ✅
│  └─ Checks expiration ✅
│  └─ Returns Jwt object ✅
│
└─ ✅ NO service calls, NO circular dependency!
```
✅ Simple, fast, no loops

#### **Step 16: Spring Security Creates Authentication**
```
Spring Security
├─ JWT decoded successfully ✅
├─ Extracts authorities from JWT scope
└─ Creates Authentication object
```
✅ Authentication established

#### **Step 17: UserController.getMyInfo() is Called**
```
UserController.getMyInfo() (Line 54)
├─ @PreAuthorize("isAuthenticated()") ✅ Passes
├─ Calls: userService.getMyInfo()
└─ Returns user information
```
✅ Method executes

#### **Step 18: UserService.getMyInfo() Executes**
```
UserService.getMyInfo() (Line 53)
├─ Gets username from SecurityContext
├─ Finds user in database
└─ Returns UserResponse
```
✅ User data retrieved

#### **Step 19: Response Flows Back**
```
Auth Service → Gateway → Client
├─ Status: 200 OK ✅
└─ Body: { "code": 1000, "result": { "id": "...", "username": "quang", ... } }
```
✅ Success!

---

## 🔑 Key Differences Summary

### BEFORE (❌ Broken):
1. **CustomJwtDecoder** called `authenticationService.introspect()`
2. This created **circular dependency** when gateway called introspect
3. JWT validation **failed** due to exception
4. Request forwarded **without authentication**
5. Auth service rejected: **401 Unauthorized**

### AFTER (✅ Fixed):
1. **CustomJwtDecoder** only validates signature/expiry (no service calls)
2. **No circular dependency** - decoder is simple and fast
3. Gateway calls introspect **separately** (business logic)
4. Introspect checks revocation in **service layer** (correct place)
5. Request forwarded **with authentication**
6. Auth service accepts: **200 OK**

---

## 📊 Visual Flow Comparison

### BEFORE:
```
Client → Gateway → Auth Service (/introspect)
                    ↓
              CustomJwtDecoder.decode()
                    ↓
         authenticationService.introspect()  ← CIRCULAR!
                    ↓
              CustomJwtDecoder.decode()  ← LOOP!
                    ↓
                  ❌ FAIL
```

### AFTER:
```
Client → Gateway → Auth Service (/introspect)
                    ↓
         AuthenticationController.introspect()
                    ↓
         AuthenticationService.introspect()
                    ↓
         AuthenticationService.verifyToken()
                    ↓
                  ✅ SUCCESS

Client → Gateway → Auth Service (/users/my-info)
                    ↓
              CustomJwtDecoder.decode()  ← Simple validation only
                    ↓
              UserController.getMyInfo()
                    ↓
                  ✅ SUCCESS
```

---

## 💡 Why This Works

1. **Separation of Concerns**:
   - `CustomJwtDecoder` = Cryptographic validation only
   - `AuthenticationService` = Business logic (revocation checks)

2. **No Circular Dependencies**:
   - Decoder doesn't call services
   - Services don't trigger decoder unnecessarily

3. **Performance**:
   - Decoder is fast (no network calls)
   - Business logic runs only when needed

4. **Correct Architecture**:
   - Each component has a single responsibility
   - Clear flow: Gateway → Service → Database

