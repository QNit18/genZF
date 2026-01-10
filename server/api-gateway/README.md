# API Gateway Service

## Overview
The API Gateway is the single entry point for all client requests to the GenZF microservices architecture. It handles JWT authentication, token validation, and request routing to downstream services.

## Architecture

### Key Features
- **JWT Validation**: Local JWT signature validation using shared secret key
- **Token Introspection**: Remote introspection to check token revocation
- **Request Routing**: Routes requests to appropriate microservices
- **CORS Configuration**: Centralized CORS handling
- **Security**: Method-level authorization support

### Port Configuration
- **Gateway**: 8888
- **Auth Service**: 8080
- **Main Service**: 8181

## Authentication Flow

```
1. Client → Gateway: Login request (POST /auth-service/auth/token)
2. Gateway → Auth Service: Forward login request
3. Auth Service → Gateway: JWT token
4. Gateway → Client: JWT token

5. Client → Gateway: Request with JWT in Authorization header
6. Gateway: Validate JWT signature locally
7. Gateway → Auth Service: Introspect token (check revocation)
8. Gateway → Downstream Service: Forward request with JWT
9. Downstream Service: Perform additional authorization
10. Response flows back through Gateway to Client
```

## Routes

### Auth Service Routes
- `POST /auth-service/users` - User registration (public)
- `POST /auth-service/auth/token` - Login (public)
- `POST /auth-service/auth/introspect` - Token introspection (public)
- `POST /auth-service/auth/logout` - Logout (public)
- `POST /auth-service/auth/refresh-token` - Refresh token (public)
- Other `/auth-service/**` routes require authentication

### Main Service Routes
- `GET /genzf/assets/**` - Asset exploration (public)
- `GET /genzf/chart-data/**` - Chart data (public)
- `GET /genzf/swagger-ui/**` - Swagger UI (public)
- `POST /genzf/portfolios/**` - Portfolio management (authenticated)
- Other `/genzf/**` routes may require authentication

## Configuration

### JWT Configuration
```yaml
jwt:
  signing-key: <base64-encoded-secret-key>
```

The signing key must be the same across all services (Gateway, Auth Service, Main Service).

### Service URLs
```yaml
auth-service:
  url: http://localhost:8080
```

## Running the Gateway

### Prerequisites
- Java 21
- Maven 3.x
- Auth Service must be running on port 8080
- Main Service must be running on port 8181

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

## Testing

### Run Tests
```bash
mvn test
```

### Integration Testing
Use tools like Postman or curl to test the authentication flow:

```bash
# 1. Register a user
curl -X POST http://localhost:8888/auth-service/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "dob": "1990-01-01"
  }'

# 2. Login and get JWT token
curl -X POST http://localhost:8888/auth-service/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# 3. Use the token to access protected endpoints
curl -X GET http://localhost:8888/genzf/portfolios/user/USER_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Security

### JWT Token Structure
```json
{
  "sub": "username",
  "iss": "qnit18.com",
  "iat": 1234567890,
  "exp": 1234571490,
  "jti": "unique-token-id",
  "scope": "ROLE_ADMIN ROLE_USER PERMISSION_NAME",
  "userId": "user-id"
}
```

### Token Validation Process
1. Extract token from `Authorization: Bearer <token>` header
2. Decode JWT and verify signature using shared secret key
3. Check token expiration
4. Call Auth Service introspect endpoint to check if token is revoked
5. Extract authorities from `scope` claim
6. Create Spring Security authentication object
7. Forward request to downstream service

## Troubleshooting

### Common Issues

#### 401 Unauthorized
- Check if JWT token is included in Authorization header
- Verify token is not expired
- Ensure token hasn't been revoked (logged out)

#### 403 Forbidden
- User doesn't have required role/permission
- Check @PreAuthorize annotations on endpoints

#### Connection Refused
- Ensure Auth Service and Main Service are running
- Verify service URLs in application.yaml

#### Invalid Signature
- Ensure all services use the same JWT signing key
- Key must be base64 encoded

## Dependencies

Key dependencies:
- Spring Cloud Gateway
- Spring Security
- Spring OAuth2 Resource Server
- Nimbus JOSE JWT
- WebFlux (reactive programming)

## Contributing

Follow the existing code style and patterns. All new endpoints should:
1. Have appropriate security configuration
2. Include unit tests
3. Update this README if adding new routes
