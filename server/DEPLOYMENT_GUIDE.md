# Deployment Guide - GenZF Microservices

## Quick Start (Development)

### Prerequisites Check

```bash
# Check Java version (should be 21)
java -version

# Check Maven version
mvn -version

# Check PostgreSQL
psql --version
```

### 1. Database Setup

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create databases
CREATE DATABASE genzf_auth;
CREATE DATABASE genzf_main;

-- Verify
\l
```

### 2. Build All Services

```bash
# From server directory
cd server

# Build auth-service
cd auth-service
mvn clean install -DskipTests
cd ..

# Build main-service
cd main-service
mvn clean install -DskipTests
cd ..

# Build api-gateway
cd api-gateway
mvn clean install -DskipTests
cd ..
```

### 3. Start Services in Order

#### Terminal 1 - Auth Service
```bash
cd server/auth-service
mvn spring-boot:run
```

Wait for: `Started AuthServiceApplication in X seconds`

#### Terminal 2 - Main Service
```bash
cd server/main-service
mvn spring-boot:run
```

Wait for: `Started MainServiceApplication in X seconds`

#### Terminal 3 - API Gateway
```bash
cd server/api-gateway
mvn spring-boot:run
```

Wait for: `Started ApiGatewayApplication in X seconds`

### 4. Verify Services

```bash
# Check all services are healthy
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Auth
curl http://localhost:8181/actuator/health  # Main
```

Expected response (all services):
```json
{
  "status": "UP",
  ...
}
```

## Testing the System

### 1. Create a Test User

```bash
curl -X POST http://localhost:8080/api/auth/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "dob": "1990-01-01"
  }'
```

### 2. Login and Get Token

```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123"
  }' | jq
```

Save the token from the response.

### 3. Access Protected Endpoint

```bash
export TOKEN="your_token_here"

curl -X GET http://localhost:8080/api/main/assets \
  -H "Authorization: Bearer $TOKEN" | jq
```

## Production Deployment

### Build JAR Files

```bash
# Build all services
cd server

cd auth-service && mvn clean package && cd ..
cd main-service && mvn clean package && cd ..
cd api-gateway && mvn clean package && cd ..
```

JAR files will be in:
- `auth-service/target/auth-service-0.0.1-SNAPSHOT.jar`
- `main-service/target/main-service-0.0.1-SNAPSHOT.jar`
- `api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar`

### Run with Production Profile

```bash
# Auth Service
java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8081

# Main Service
java -jar main-service/target/main-service-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8181

# API Gateway
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8080
```

### Environment Variables for Production

Create `.env` file:

```bash
# Database Configuration
DB_HOST=your-db-host
DB_PORT=5432
AUTH_DB_NAME=genzf_auth
MAIN_DB_NAME=genzf_main
DB_USERNAME=postgres
DB_PASSWORD=your-secure-password

# JWT Configuration
JWT_SIGNING_KEY=your-secure-signing-key-here

# Service URLs
AUTH_SERVICE_URL=http://auth-service:8081
MAIN_SERVICE_URL=http://main-service:8181

# Logging
LOG_LEVEL=INFO
```

## Docker Deployment (Future)

### Dockerfile Example (api-gateway)

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose (Future)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  auth-service:
    build: ./auth-service
    ports:
      - "8081:8081"
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/genzf_auth

  main-service:
    build: ./main-service
    ports:
      - "8181:8181"
    depends_on:
      - postgres
      - auth-service
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/genzf_main
      SERVICES_AUTH_URL: http://auth-service:8081

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - auth-service
      - main-service
    environment:
      SERVICES_AUTH_URL: http://auth-service:8081
      SERVICES_MAIN_URL: http://main-service:8181

volumes:
  postgres-data:
```

## Monitoring and Maintenance

### Check Service Health

```bash
# Script to check all services
#!/bin/bash
echo "Checking API Gateway..."
curl -s http://localhost:8080/actuator/health | jq .status

echo "Checking Auth Service..."
curl -s http://localhost:8081/actuator/health | jq .status

echo "Checking Main Service..."
curl -s http://localhost:8181/actuator/health | jq .status
```

### View Logs

```bash
# If running with spring-boot:run
# Logs appear in the terminal

# If running JAR files
java -jar app.jar > app.log 2>&1 &
tail -f app.log
```

### Graceful Shutdown

```bash
# Send SIGTERM signal
kill -15 <PID>

# Or use actuator shutdown endpoint (if enabled)
curl -X POST http://localhost:8080/actuator/shutdown
```

## Troubleshooting

### Service won't start

1. Check port availability:
```bash
netstat -an | grep 8080
netstat -an | grep 8081
netstat -an | grep 8181
```

2. Check logs for errors
3. Verify database connectivity

### Gateway can't reach services

1. Verify services are running:
```bash
ps aux | grep java
```

2. Check service health:
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8181/actuator/health
```

3. Check firewall rules

### Database connection errors

1. Verify PostgreSQL is running:
```bash
pg_isready -h localhost -p 5432
```

2. Check database exists:
```bash
psql -U postgres -l
```

3. Verify credentials in application.yaml

### JWT token errors

1. Ensure all services use same signing key
2. Check token expiration
3. Verify token format (Bearer prefix)

## Performance Tuning

### JVM Options

```bash
java -Xms512m -Xmx1024m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar app.jar
```

### Database Connection Pool

Update `application.yaml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

### Circuit Breaker Tuning

```yaml
resilience4j:
  circuitbreaker:
    instances:
      authService:
        slidingWindowSize: 20
        failureRateThreshold: 60
```

## Backup and Recovery

### Database Backup

```bash
# Backup auth database
pg_dump -U postgres genzf_auth > auth_backup.sql

# Backup main database
pg_dump -U postgres genzf_main > main_backup.sql
```

### Database Restore

```bash
# Restore auth database
psql -U postgres genzf_auth < auth_backup.sql

# Restore main database
psql -U postgres genzf_main < main_backup.sql
```

## Scaling

### Horizontal Scaling

1. Run multiple instances of each service
2. Use load balancer (nginx, HAProxy)
3. Implement service discovery
4. Use shared database or read replicas

### Vertical Scaling

1. Increase JVM heap size
2. Optimize database queries
3. Add database indexes
4. Enable caching (Redis)

## Security Checklist

- [ ] Change default JWT signing key
- [ ] Use strong database passwords
- [ ] Enable HTTPS in production
- [ ] Implement rate limiting
- [ ] Enable security headers
- [ ] Regular security updates
- [ ] Monitor for suspicious activity
- [ ] Implement API versioning
- [ ] Use secrets management (Vault)

## Support

For issues and questions:
1. Check logs for error messages
2. Verify configuration
3. Review this deployment guide
4. Check service health endpoints
5. Consult project README.md

