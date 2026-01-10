# Quick Start Guide - GenZF with JWT Authentication

## Prerequisites
- Java 21
- Maven 3.x
- PostgreSQL with databases: `auth-service` and `genzf`

## 1. Start All Services

### Terminal 1: Auth Service
```bash
cd server/auth-service
mvn spring-boot:run
```
✅ Running on http://localhost:8080

### Terminal 2: Main Service
```bash
cd server/main-service
mvn spring-boot:run
```
✅ Running on http://localhost:8181

### Terminal 3: API Gateway
```bash
cd server/api-gateway
mvn spring-boot:run
```
✅ Running on http://localhost:8888

## 2. Test the Authentication Flow

### Step 1: Register a New User
```bash
curl -X POST http://localhost:8888/auth-service/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01"
  }'
```

**Expected Response**:
```json
{
  "code": 1000,
  "result": {
    "id": "uuid-here",
    "username": "john",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01"
  }
}
```

### Step 2: Login and Get JWT Token
```bash
curl -X POST http://localhost:8888/auth-service/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

**Expected Response**:
```json
{
  "code": 1000,
  "result": {
    "authenticated": true,
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huIiwiaXNzIjoicW5pdDE4LmNvbSIsImV4cCI6MTcwNTI0OTIwMCwiaWF0IjoxNzA1MjQ1NjAwLCJqdGkiOiJ1dWlkLWhlcmUiLCJzY29wZSI6IlJPTEVfVVNFUiIsInVzZXJJZCI6InV1aWQtaGVyZSJ9.signature"
  }
}
```

**Copy the token from the response!**

### Step 3: Get User Info (Protected Endpoint)
```bash
# Replace YOUR_JWT_TOKEN with the token from step 2
curl -X GET http://localhost:8888/auth-service/users/my-info \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response**:
```json
{
  "code": 1000,
  "result": {
    "id": "uuid-here",
    "username": "john",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01",
    "roles": ["USER"]
  }
}
```

### Step 4: Create a Portfolio (Protected Endpoint)
```bash
# Replace YOUR_JWT_TOKEN and USER_ID
curl -X POST http://localhost:8888/genzf/portfolios \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "USER_ID_FROM_STEP_3",
    "cashBalance": 10000.00
  }'
```

**Expected Response**:
```json
{
  "code": 1000,
  "result": {
    "id": "portfolio-uuid",
    "userId": "user-uuid",
    "cashBalance": 10000.00,
    "netWorth": 10000.00
  }
}
```

### Step 5: Access Public Endpoints (No Token Required)
```bash
# Get all assets (public)
curl -X GET http://localhost:8888/genzf/assets

# Get asset by symbol (public)
curl -X GET http://localhost:8888/genzf/assets/symbol/XAU-USD

# Get home assets (public)
curl -X GET http://localhost:8888/genzf/assets/home
```

### Step 6: Test Admin Endpoints (Requires ADMIN Role)
```bash
# This will fail with 403 if user is not ADMIN
curl -X POST http://localhost:8888/genzf/assets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gold",
    "symbol": "XAU/USD",
    "category": "GOLD",
    "currentPrice": 2050.00
  }'
```

### Step 7: Logout
```bash
curl -X POST http://localhost:8888/auth-service/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"token": "YOUR_JWT_TOKEN"}'
```

**After logout, the token becomes invalid immediately.**

## 3. Architecture Overview

```
┌────────────┐
│   Client   │
└─────┬──────┘
      │
      ▼
┌──────────────────┐
│  API Gateway     │  Port 8888
│  (Entry Point)   │
└────┬───────┬─────┘
     │       │
     ▼       ▼
┌─────────┐ ┌─────────┐
│  Auth   │ │  Main   │
│ Service │ │ Service │
│  8080   │ │  8181   │
└─────────┘ └─────────┘
```

## 4. Key Endpoints

### Auth Service (via Gateway)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth-service/users` | No | Register user |
| POST | `/auth-service/auth/token` | No | Login |
| POST | `/auth-service/auth/introspect` | No | Validate token |
| POST | `/auth-service/auth/logout` | No | Logout |
| POST | `/auth-service/auth/refresh-token` | No | Refresh token |
| GET | `/auth-service/users/my-info` | Yes | Get current user |
| GET | `/auth-service/users` | Admin | List all users |

### Main Service (via Gateway)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/genzf/assets` | No | List assets |
| GET | `/genzf/assets/{id}` | No | Get asset |
| POST | `/genzf/assets` | Admin | Create asset |
| PUT | `/genzf/assets/{id}` | Admin | Update asset |
| DELETE | `/genzf/assets/{id}` | Admin | Delete asset |
| GET | `/genzf/portfolios/user/{userId}` | Yes | Get user portfolio |
| POST | `/genzf/portfolios` | Yes | Create portfolio |
| DELETE | `/genzf/portfolios/{id}` | Yes | Delete portfolio |

## 5. JWT Token Structure

```json
{
  "sub": "username",
  "iss": "qnit18.com",
  "iat": 1705245600,
  "exp": 1705249200,
  "jti": "unique-uuid",
  "scope": "ROLE_USER ROLE_ADMIN",
  "userId": "user-uuid"
}
```

## 6. Environment Variables (Optional)

```bash
# JWT Configuration
export JWT_SIGNING_KEY=fbX2a4nQ4tdMnfExFUl+uA9aD9IFS+csS8GP96pR75RxrCiUcEYvpn+b4wWsgJshvXMUQiDUxhEBxA9RdPj+OQ==

# Database Configuration
export DB_URL=jdbc:postgresql://localhost:5432/auth-service
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

## 7. Common Issues

### 503 Service Unavailable
**Problem**: Gateway can't reach services  
**Solution**: Ensure Auth Service and Main Service are running

### 401 Unauthorized
**Problem**: Invalid or expired token  
**Solution**: Login again to get a new token

### 403 Forbidden
**Problem**: User lacks required permissions  
**Solution**: Check user roles and endpoint requirements

### Connection Refused
**Problem**: Service not running  
**Solution**: Start all three services in correct order

## 8. Swagger UI

Access API documentation:
- Main Service: http://localhost:8888/genzf/swagger-ui.html

## 9. Next Steps

1. ✅ Explore the API using Swagger UI
2. ✅ Test different user roles (USER vs ADMIN)
3. ✅ Try the token refresh flow
4. ✅ Test logout and verify token is invalidated
5. ✅ Build a frontend application

## 10. Production Deployment

For production:
1. Use HTTPS for all services
2. Store JWT signing key in secure vault
3. Use environment-specific configurations
4. Enable rate limiting on Gateway
5. Set up monitoring and logging
6. Use PostgreSQL with proper security

## 11. Development Tips

### Postman Collection
Create a Postman collection with:
1. Register User
2. Login (save token to environment variable)
3. Get My Info (use saved token)
4. Create Portfolio (use saved token)
5. Logout

### IntelliJ IDEA
- Run all three services in separate run configurations
- Use HTTP Client plugin for testing
- Enable hot reload for faster development

### VSCode
- Use REST Client extension
- Create `.http` files for testing
- Use Spring Boot Dashboard extension

## 12. Documentation

For detailed information:
- **Architecture**: See `server/ARCHITECTURE.md`
- **Implementation**: See `server/IMPLEMENTATION_SUMMARY.md`
- **Gateway**: See `server/api-gateway/README.md`

## Support

For issues or questions:
1. Check the documentation files
2. Review the test files for examples
3. Check application logs for errors

---

**Happy Coding! 🚀**
