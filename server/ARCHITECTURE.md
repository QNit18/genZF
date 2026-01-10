# GenZF Microservices Architecture

## Overview
GenZF follows a microservices architecture pattern with API Gateway, Authentication Service, and Main Service, implementing JWT-based authentication and authorization similar to the Bookteria project.

## Architecture Diagram

```
┌──────────┐
│  Client  │
│(Frontend)│
└────┬─────┘
     │
     │ HTTP Requests
     │ (with JWT token)
     ▼
┌────────────────────┐
│   API Gateway      │
│   (Port 8888)      │
│                    │
│ - JWT Validation   │
│ - Token Introspect │
│ - Request Routing  │
│ - CORS Handling    │
└────┬───────────┬───┘
     │           │
     │           │
     ▼           ▼
┌──────────┐  ┌─────────────┐
│  Auth    │  │    Main     │
│ Service  │  │   Service   │
│(Port 8080)  │(Port 8181)  │
│           │  │             │
│- Login    │  │- Portfolios │
│- Register │  │- Assets     │
│- JWT Gen  │  │- Business   │
│- Introspect  │  Logic      │
└──────────┘  └─────────────┘
```

## Services

### 1. API Gateway (Port 8888)
**Purpose**: Single entry point for all client requests

**Responsibilities**:
- Validate JWT tokens locally (signature verification)
- Check token revocation via Auth Service introspection
- Route requests to appropriate services
- Handle CORS configuration
- Extract and propagate authentication context

**Technology Stack**:
- Spring Cloud Gateway (Reactive)
- Spring Security
- Spring OAuth2 Resource Server
- WebFlux

**Key Routes**:
- `/auth-service/**` → Auth Service
- `/genzf/**` → Main Service

### 2. Auth Service (Port 8080)
**Purpose**: Handle user authentication and authorization

**Responsibilities**:
- User registration and management
- JWT token generation
- Token introspection (validation and revocation check)
- Token refresh
- User logout (token invalidation)
- Role and permission management

**Technology Stack**:
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Nimbus JOSE JWT

**Database**: `auth-service` PostgreSQL database

**Entities**:
- User
- Role
- Permission
- InvalidedToken

### 3. Main Service (Port 8181)
**Purpose**: Core business logic for GenZF application

**Responsibilities**:
- Asset management (Gold, Bitcoin, Forex)
- Portfolio management
- Budget rules
- Chart data
- User-specific business operations

**Technology Stack**:
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Swagger/OpenAPI

**Database**: `genzf` PostgreSQL database

**Entities**:
- Asset
- Portfolio
- AssetUser

## Authentication & Authorization Flow

### 1. User Registration Flow
```
Client → Gateway → Auth Service
  POST /auth-service/users
  {
    "username": "john",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01"
  }
```

### 2. Login Flow
```
1. Client → Gateway: POST /auth-service/auth/token
   Body: { "username": "john", "password": "password123" }

2. Gateway → Auth Service: Forward request

3. Auth Service:
   - Validate credentials
   - Generate JWT with claims:
     * sub: username
     * userId: user ID
     * scope: "ROLE_USER ROLE_ADMIN PERMISSION_NAME"
     * iss: "qnit18.com"
     * exp: current time + 3600s
     * jti: unique token ID

4. Auth Service → Gateway → Client: Return JWT token
```

### 3. Protected Request Flow
```
1. Client → Gateway: GET /genzf/portfolios/user/{userId}
   Headers: Authorization: Bearer <JWT_TOKEN>

2. Gateway JWT Filter:
   a. Extract token from Authorization header
   b. Decode JWT and verify signature (local validation)
   c. Check expiration time
   d. Call Auth Service introspect endpoint
   e. If valid, extract authorities from 'scope' claim
   f. Create Spring Security Authentication object

3. Gateway → Main Service: Forward request with JWT

4. Main Service:
   a. Decode JWT using shared secret key
   b. Extract user context (userId, roles, permissions)
   c. Perform method-level authorization (@PreAuthorize)
   d. Execute business logic

5. Main Service → Gateway → Client: Return response
```

### 4. Token Introspection
```
Gateway → Auth Service: POST /auth-service/auth/introspect
  Body: { "token": "jwt.token.here" }

Auth Service checks:
  - Token signature valid?
  - Token not expired?
  - Token not in InvalidedToken table (logged out)?

Response: { "valid": true/false }
```

### 5. Token Refresh Flow
```
Client → Gateway → Auth Service: POST /auth-service/auth/refresh-token
  Body: { "token": "existing.jwt.token" }

Auth Service:
  - Verify token is within refreshable duration
  - Invalidate old token (add to InvalidedToken table)
  - Generate new token with fresh expiration

Response: { "authenticated": true, "token": "new.jwt.token" }
```

### 6. Logout Flow
```
Client → Gateway → Auth Service: POST /auth-service/auth/logout
  Body: { "token": "jwt.token.here" }

Auth Service:
  - Extract JTI (JWT ID) from token
  - Add to InvalidedToken table
  - Token becomes invalid immediately

Response: Success
```

## JWT Token Structure

### Claims
```json
{
  "sub": "username",           // Subject - username
  "iss": "qnit18.com",          // Issuer
  "iat": 1234567890,            // Issued At (timestamp)
  "exp": 1234571490,            // Expiration (timestamp)
  "jti": "uuid-here",           // JWT ID (unique identifier)
  "scope": "ROLE_ADMIN ROLE_USER PERMISSION_READ PERMISSION_WRITE",
  "userId": "user-uuid-here"    // User ID for business logic
}
```

### Scope Format
- Roles: `ROLE_<ROLE_NAME>` (e.g., `ROLE_ADMIN`, `ROLE_USER`)
- Permissions: `PERMISSION_NAME` (e.g., `READ_ASSET`, `WRITE_PORTFOLIO`)

## Security Configuration

### Public Endpoints (No Authentication Required)
- `POST /auth-service/users` - User registration
- `POST /auth-service/auth/token` - Login
- `POST /auth-service/auth/introspect` - Token introspection
- `POST /auth-service/auth/logout` - Logout
- `POST /auth-service/auth/refresh-token` - Token refresh
- `GET /genzf/assets/**` - Asset exploration
- `GET /genzf/chart-data/**` - Chart data
- `GET /genzf/swagger-ui/**` - Swagger UI
- `GET /genzf/api-docs/**` - API documentation

### Protected Endpoints (Authentication Required)
- All other endpoints require valid JWT token
- Method-level authorization using `@PreAuthorize` annotations

### Authorization Examples

#### Auth Service
```java
@GetMapping("/{userId}")
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
UserResponse getUser(@PathVariable String userId)

@DeleteMapping("/{userId}")
@PreAuthorize("hasRole('ADMIN')")
String deleteUser(@PathVariable String userId)

@GetMapping("/my-info")
@PreAuthorize("isAuthenticated()")
UserResponse getMyInfo()
```

#### Main Service
```java
@PostMapping("/portfolios")
@PreAuthorize("isAuthenticated()")
PortfolioResponse createPortfolio(...)

@PostMapping("/assets")
@PreAuthorize("hasRole('ADMIN')")
AssetResponse createAsset(...)

@DeleteMapping("/assets/{id}")
@PreAuthorize("hasRole('ADMIN')")
String deleteAsset(@PathVariable UUID id)
```

## Configuration

### Shared JWT Configuration
All services must use the same JWT signing key:

```yaml
jwt:
  signing-key: <base64-encoded-512-bit-key>
  valid-duration: 3600        # 1 hour
  refreshable-duration: 36000 # 10 hours
```

### Service URLs
```yaml
# API Gateway
server:
  port: 8888

# Auth Service
server:
  port: 8080
  servlet:
    context-path: /auth-service

# Main Service
server:
  port: 8181
  servlet:
    context-path: /genzf
```

## Database Schema

### Auth Service Database
- **users**: User accounts
- **roles**: Role definitions (ADMIN, USER, etc.)
- **permissions**: Permission definitions
- **user_roles**: User-Role mapping (Many-to-Many)
- **role_permissions**: Role-Permission mapping (Many-to-Many)
- **invalided_token**: Revoked tokens (for logout)

### Main Service Database
- **assets**: Trading assets (Gold, Bitcoin, Forex)
- **portfolios**: User portfolios
- **asset_users**: User-Asset relationship

## Running the Services

### Prerequisites
- Java 21
- Maven 3.x
- PostgreSQL databases: `auth-service` and `genzf`

### Startup Order
1. Start Auth Service (Port 8080)
2. Start Main Service (Port 8181)
3. Start API Gateway (Port 8888)

### Build All Services
```bash
cd server
mvn clean install
```

### Run Individual Services
```bash
# Auth Service
cd auth-service
mvn spring-boot:run

# Main Service
cd main-service
mvn spring-boot:run

# API Gateway
cd api-gateway
mvn spring-boot:run
```

## Testing the Flow

### 1. Register User
```bash
curl -X POST http://localhost:8888/auth-service/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "dob": "1990-01-01"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8888/auth-service/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

Response:
```json
{
  "code": 1000,
  "result": {
    "authenticated": true,
    "token": "eyJhbGc..."
  }
}
```

### 3. Access Protected Endpoint
```bash
curl -X GET http://localhost:8888/genzf/portfolios/user/USER_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Logout
```bash
curl -X POST http://localhost:8888/auth-service/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"token": "YOUR_JWT_TOKEN"}'
```

## Best Practices

### 1. JWT Token Management
- Keep tokens short-lived (1 hour)
- Implement refresh token mechanism
- Store tokens securely on client side
- Never expose signing key

### 2. Security
- Use HTTPS in production
- Implement rate limiting on Gateway
- Log security events
- Regularly rotate JWT signing keys

### 3. Error Handling
- Return appropriate HTTP status codes
- Don't leak sensitive information in errors
- Log all authentication failures

### 4. Performance
- Cache introspection results (with short TTL)
- Use connection pooling for database
- Monitor Gateway performance

## Troubleshooting

### Common Issues

1. **401 Unauthorized**
   - Check JWT token is present and valid
   - Verify token not expired
   - Ensure token not revoked

2. **403 Forbidden**
   - User lacks required role/permission
   - Check @PreAuthorize annotations

3. **Invalid Signature**
   - All services must use same signing key
   - Key must be properly base64 encoded

4. **Connection Issues**
   - Verify all services are running
   - Check service URLs in configuration

## Future Enhancements

1. Service Discovery (Eureka/Consul)
2. Circuit Breaker (Resilience4j)
3. Distributed Tracing (Sleuth/Zipkin)
4. API Rate Limiting
5. Redis for token blacklist
6. OAuth2 Social Login
7. Two-Factor Authentication
