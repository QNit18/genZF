# Quick Reference Guide - GenZF Microservices

## Service Ports

| Service | Port | Database |
|---------|------|----------|
| API Gateway | 8080 | - |
| Auth Service | 8081 | genzf_auth |
| Main Service | 8181 | genzf_main |

## Quick Start Commands

```bash
# Start services (3 separate terminals)
cd server/auth-service && mvn spring-boot:run
cd server/main-service && mvn spring-boot:run
cd server/api-gateway && mvn spring-boot:run
```

## Health Check URLs

```bash
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8181/actuator/health  # Main
```

## Common API Calls

### Authentication

#### Register User
```bash
POST http://localhost:8080/api/auth/users
Content-Type: application/json

{
  "username": "john_doe",
  "password": "Password123!",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "dob": "1990-01-01"
}
```

#### Login (Get Token)
```bash
POST http://localhost:8080/api/auth/token
Content-Type: application/json

{
  "username": "john_doe",
  "password": "Password123!"
}
```

#### Refresh Token
```bash
POST http://localhost:8080/api/auth/refresh-token
Content-Type: application/json

{
  "token": "your_refresh_token"
}
```

#### Logout
```bash
POST http://localhost:8080/api/auth/logout
Content-Type: application/json

{
  "token": "your_jwt_token"
}
```

### Protected Endpoints (Require JWT)

#### Get All Assets
```bash
GET http://localhost:8080/api/main/assets
Authorization: Bearer YOUR_JWT_TOKEN
```

#### Get Asset by ID
```bash
GET http://localhost:8080/api/main/assets/{id}
Authorization: Bearer YOUR_JWT_TOKEN
```

#### Create Asset
```bash
POST http://localhost:8080/api/main/assets
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "symbol": "BTC/USD",
  "name": "Bitcoin",
  "category": "CRYPTO",
  "currentPrice": 45000.00,
  "previousClose": 44000.00
}
```

#### Get Portfolio
```bash
GET http://localhost:8080/api/main/portfolios
Authorization: Bearer YOUR_JWT_TOKEN
```

## Environment Variables

```bash
# Auth Service
export SERVER_PORT=8081
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/genzf_auth
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres

# Main Service
export SERVER_PORT=8181
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/genzf_main
export SERVICES_AUTH_URL=http://localhost:8081

# API Gateway
export SERVER_PORT=8080
export SERVICES_AUTH_URL=http://localhost:8081
export SERVICES_MAIN_URL=http://localhost:8181
```

## Database Setup

```sql
-- Create databases
CREATE DATABASE genzf_auth;
CREATE DATABASE genzf_main;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE genzf_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE genzf_main TO postgres;
```

## Troubleshooting Commands

### Check if services are running
```bash
ps aux | grep java
```

### Check port usage
```bash
netstat -an | grep 8080
netstat -an | grep 8081
netstat -an | grep 8181
```

### Kill service by port
```bash
lsof -ti:8080 | xargs kill -9
lsof -ti:8081 | xargs kill -9
lsof -ti:8181 | xargs kill -9
```

### View service logs
```bash
# If running with mvn spring-boot:run, logs are in terminal

# If running JAR
tail -f logs/application.log
```

### Check PostgreSQL connection
```bash
psql -U postgres -d genzf_auth -c "SELECT 1"
psql -U postgres -d genzf_main -c "SELECT 1"
```

## Configuration Files

| Service | Configuration File |
|---------|-------------------|
| API Gateway | `server/api-gateway/src/main/resources/application.yaml` |
| Auth Service | `server/auth-service/src/main/resources/application.yaml` |
| Main Service | `server/main-service/src/main/resources/application.yaml` |

## Request Flow

```
Client Request
    ↓
API Gateway (8080)
    ↓ (validates JWT)
    ↓
/api/auth/** → Auth Service (8081)
/api/main/** → Main Service (8181)
    ↓
Response
    ↓
Client
```

## Circuit Breaker States

| State | Meaning |
|-------|---------|
| CLOSED | Normal operation |
| OPEN | Too many failures, blocking calls |
| HALF_OPEN | Testing if service recovered |

Check circuit breaker state:
```bash
curl http://localhost:8181/actuator/health | jq '.components.authServiceHealthIndicator.details."circuit-breaker"'
```

## Common HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized (invalid/missing JWT) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 500 | Internal Server Error |
| 503 | Service Unavailable |

## JWT Token Structure

```
Header.Payload.Signature

Payload contains:
- sub: username
- scope: user roles/permissions
- iat: issued at timestamp
- exp: expiration timestamp
```

## Swagger API Documentation

Main Service Swagger UI:
- Via Gateway: `http://localhost:8080/api/main/swagger-ui.html`
- Direct: `http://localhost:8181/swagger-ui.html`

## Maven Commands

```bash
# Clean and build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Run service
mvn spring-boot:run

# Build JAR
mvn clean package

# Run tests
mvn test
```

## Docker Commands (Future)

```bash
# Build image
docker build -t genf-api-gateway .

# Run container
docker run -p 8080:8080 genf-api-gateway

# Docker compose
docker-compose up -d
docker-compose down
```

## Monitoring Endpoints

```bash
# Health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info
```

## Security Best Practices

1. ✅ Always use HTTPS in production
2. ✅ Rotate JWT signing keys regularly
3. ✅ Use strong database passwords
4. ✅ Enable rate limiting
5. ✅ Monitor for suspicious activity
6. ✅ Keep dependencies updated
7. ✅ Use environment variables for secrets

## Useful cURL Examples

### Test with saved token
```bash
# Save token
export TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"Test@123"}' \
  | jq -r '.result.token')

# Use token
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/main/assets
```

### Pretty print JSON responses
```bash
curl http://localhost:8080/api/main/assets | jq
```

### Check response headers
```bash
curl -I http://localhost:8080/actuator/health
```

### Time request
```bash
time curl http://localhost:8080/api/main/assets
```

## Common Errors and Solutions

| Error | Solution |
|-------|----------|
| Port already in use | Kill existing process or change port |
| Database connection failed | Check PostgreSQL is running |
| JWT validation failed | Ensure same signing key across services |
| 503 Service Unavailable | Check downstream services are running |
| Circuit breaker OPEN | Wait 5 seconds or restart auth-service |

## Performance Tips

1. Use connection pooling (already configured)
2. Enable database indexes
3. Cache frequently accessed data
4. Monitor circuit breaker metrics
5. Adjust JVM heap size for production

## Support Resources

- **README.md** - Architecture overview
- **DEPLOYMENT_GUIDE.md** - Deployment instructions
- **REFACTORING_SUMMARY.md** - What was changed
- **Swagger UI** - API documentation

---

**Last Updated**: 2024
**Version**: 1.0.0

