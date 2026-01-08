# GenZF Microservices Architecture

## Overview

This project implements a microservices architecture with three core services:
- **API Gateway** (Port 8080) - Single entry point for all client requests
- **Auth Service** (Port 8081) - Authentication and user management
- **Main Service** (Port 8181) - Core business logic for financial assets

## Architecture Diagram

```
Client → API Gateway (8080) → Auth Service (8081)
                            → Main Service (8181) → Auth Service (internal)
```

## Technology Stack

- **Spring Boot**: 3.3.5
- **Java**: 21
- **Spring Cloud Gateway MVC**: Routing and filtering
- **Spring Security**: JWT-based authentication
- **Resilience4j**: Circuit breaker and retry patterns
- **Spring Boot Actuator**: Health checks and monitoring
- **PostgreSQL**: Database
- **Maven**: Build tool

## Services Overview

### 1. API Gateway (Port 8080)

**Entry point for all external requests**

- **Routes**:
  - `/api/auth/**` → Auth Service (port 8081)
  - `/api/main/**` → Main Service (port 8181)
  
- **Features**:
  - JWT validation for all protected endpoints
  - Request/response logging
  - CORS configuration
  - Health check aggregation
  - Error handling and transformation

- **Public Endpoints** (No JWT required):
  - `POST /api/auth/token` - Login
  - `POST /api/auth/register` - Register new user
  - `POST /api/auth/introspect` - Validate token
  - `POST /api/auth/refresh-token` - Refresh JWT token
  - `GET /actuator/health` - Health check

### 2. Auth Service (Port 8081)

**User authentication and authorization**

- **Endpoints**:
  - `POST /auth/token` - Login
  - `POST /auth/introspect` - Validate token
  - `POST /auth/logout` - Logout
  - `POST /auth/refresh-token` - Refresh token
  - `GET /users` - List users
  - `GET /users/{id}` - Get user by ID
  - `POST /users` - Create user
  - `PUT /users/{id}` - Update user
  - `DELETE /users/{id}` - Delete user
  - Role and permission management endpoints

- **Database**: `genzf_auth`

### 3. Main Service (Port 8181)

**Core business logic for financial assets and portfolios**

- **Endpoints**:
  - Asset management (`/assets/**`)
  - Portfolio management (`/portfolios/**`)
  - Budget rules (`/budget-rules/**`)
  - Chart data (`/chart-data/**`)

- **Features**:
  - Circuit breaker for auth-service calls
  - Retry logic with exponential backoff
  - Swagger API documentation at `/swagger-ui.html`

- **Database**: `genzf_main`

## Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL 12+
- Two PostgreSQL databases: `genzf_auth` and `genzf_main`

## Database Setup

```sql
-- Create databases
CREATE DATABASE genzf_auth;
CREATE DATABASE genzf_main;

-- Create user (if needed)
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE genzf_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE genzf_main TO postgres;
```

## Running the Services

### Option 1: Run All Services Individually

1. **Start Auth Service**:
```bash
cd server/auth-service
mvn clean install
mvn spring-boot:run
```

2. **Start Main Service**:
```bash
cd server/main-service
mvn clean install
mvn spring-boot:run
```

3. **Start API Gateway**:
```bash
cd server/api-gateway
mvn clean install
mvn spring-boot:run
```

### Option 2: Build and Run JARs

```bash
# Build all services
cd server/auth-service && mvn clean package
cd ../main-service && mvn clean package
cd ../api-gateway && mvn clean package

# Run services
java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar
java -jar main-service/target/main-service-0.0.1-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
```

## Service Startup Order

1. **Auth Service** (8081) - Start first
2. **Main Service** (8181) - Depends on Auth Service
3. **API Gateway** (8080) - Start last (routes to other services)

## API Usage Examples

### 1. Register a New User (via Gateway)

```bash
curl -X POST http://localhost:8080/api/auth/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "Password123!",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01"
  }'
```

### 2. Login and Get JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "Password123!"
  }'
```

Response:
```json
{
  "code": 1000,
  "message": "Success",
  "result": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "authenticated": true
  }
}
```

### 3. Access Protected Endpoint (with JWT)

```bash
curl -X GET http://localhost:8080/api/main/assets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Check Service Health

```bash
# Gateway health (includes downstream services)
curl http://localhost:8080/actuator/health

# Auth Service health
curl http://localhost:8081/actuator/health

# Main Service health
curl http://localhost:8181/actuator/health
```

## Configuration

### Environment Variables

You can override default configurations using environment variables:

```bash
# Auth Service
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/genzf_auth
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export SERVER_PORT=8081

# Main Service
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/genzf_main
export SERVICES_AUTH_URL=http://localhost:8081
export SERVER_PORT=8181

# API Gateway
export SERVER_PORT=8080
export SERVICES_AUTH_URL=http://localhost:8081
export SERVICES_MAIN_URL=http://localhost:8181
```

### Application Properties

Key configuration files:
- `server/api-gateway/src/main/resources/application.yaml`
- `server/auth-service/src/main/resources/application.yaml`
- `server/main-service/src/main/resources/application.yaml`

## Resilience Patterns

### Circuit Breaker (Main Service → Auth Service)

- **Sliding Window Size**: 10 calls
- **Failure Rate Threshold**: 50%
- **Wait Duration in Open State**: 5 seconds
- **Half-Open State**: 3 permitted calls

### Retry Logic

- **Max Attempts**: 3
- **Wait Duration**: 1 second
- **Exponential Backoff**: Enabled (multiplier: 2)

## Monitoring and Health Checks

### Actuator Endpoints

All services expose the following actuator endpoints:

- `/actuator/health` - Overall health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Custom Health Indicators

1. **API Gateway**: Monitors downstream services (auth-service, main-service)
2. **Auth Service**: Database connection health
3. **Main Service**: Database health and auth-service connectivity

## API Documentation

Main Service provides Swagger UI for API documentation:
- **Swagger UI**: http://localhost:8181/swagger-ui.html
- **API Docs**: http://localhost:8181/api-docs

When accessing through the gateway:
- **Via Gateway**: http://localhost:8080/api/main/swagger-ui.html

## Security

### JWT Configuration

- **Algorithm**: HS512 (HMAC with SHA-512)
- **Token Validity**: 3600 seconds (1 hour)
- **Refresh Token Validity**: 36000 seconds (10 hours)

### Gateway Security

- Validates JWT tokens for all protected endpoints
- Public endpoints bypass JWT validation
- Extracts user information and authorities from JWT

### Service-to-Service Communication

- Main Service can call Auth Service directly (bypass gateway)
- Internal calls use the same JWT validation
- Circuit breaker protects against auth-service failures

## Troubleshooting

### Issue: Services can't connect to PostgreSQL

**Solution**: Check database configuration and ensure PostgreSQL is running
```bash
psql -U postgres -c "SELECT version();"
```

### Issue: Gateway returns 503 Service Unavailable

**Solution**: Ensure downstream services (auth-service, main-service) are running
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8181/actuator/health
```

### Issue: JWT validation fails

**Solution**: Ensure all services use the same JWT signing key in `application.yaml`

### Issue: Circuit breaker is OPEN

**Solution**: Check auth-service logs and health. Wait for circuit breaker to transition to HALF_OPEN state (5 seconds)

## Project Structure

```
server/
├── api-gateway/
│   ├── src/main/java/com/qnit18/api_gateway/
│   │   ├── config/          # Gateway and security configuration
│   │   ├── filter/          # JWT and logging filters
│   │   ├── exception/       # Error handling
│   │   ├── dto/             # DTOs
│   │   └── health/          # Health indicators
│   └── src/main/resources/
│       └── application.yaml
├── auth-service/
│   ├── src/main/java/com/qnit18/auth_service/
│   │   ├── configuration/   # Security and JWT config
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access
│   │   ├── entity/          # JPA entities
│   │   ├── dto/             # Request/Response DTOs
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── exception/       # Error handling
│   │   └── health/          # Health indicators
│   └── src/main/resources/
│       └── application.yaml
└── main-service/
    ├── src/main/java/com/qnit18/main_service/
    │   ├── configuration/   # Configuration classes
    │   ├── controller/      # REST controllers
    │   ├── service/         # Business logic
    │   ├── repository/      # Data access
    │   ├── entity/          # JPA entities
    │   ├── dto/             # Request/Response DTOs
    │   ├── mapper/          # MapStruct mappers
    │   ├── exception/       # Error handling
    │   ├── client/          # Auth service client
    │   └── health/          # Health indicators
    └── src/main/resources/
        └── application.yaml
```

## Best Practices

1. **Always route external requests through API Gateway**
2. **Use JWT tokens for authentication**
3. **Monitor circuit breaker states in production**
4. **Check health endpoints before deployment**
5. **Use environment-specific configuration files**
6. **Keep JWT signing keys secure**
7. **Implement proper logging for troubleshooting**

## Future Enhancements

- [ ] Service discovery (Eureka, Consul)
- [ ] Distributed tracing (Zipkin, Jaeger)
- [ ] Centralized configuration (Spring Cloud Config)
- [ ] API rate limiting
- [ ] Docker containerization
- [ ] Kubernetes orchestration
- [ ] Load balancing for multiple instances
- [ ] Message queue integration (RabbitMQ, Kafka)

## Contributing

When adding new services:
1. Follow the existing project structure
2. Add health indicators
3. Configure circuit breakers for external calls
4. Update API Gateway routes
5. Document endpoints and configuration

## License

This project is part of the GenZF financial management system.

