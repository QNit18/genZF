# GenZF Server System - Beginner's Guide

**Welcome to the GenZF project!** This guide will help you understand the entire system from scratch, even if you're new to microservices, Spring Boot, or backend development.

---

## Table of Contents

1. [Introduction](#1-introduction--overview)
2. [System Architecture](#2-system-architecture)
3. [Services Deep Dive](#3-services-deep-dive)
4. [Key Concepts Explained](#4-key-concepts-explained)
5. [Request Flow Examples](#5-request-flow-examples)
6. [Project Structure](#6-project-structure)
7. [Setup & Installation](#7-setup--installation)
8. [Development Workflow](#8-development-workflow)
9. [Common Tasks](#9-common-tasks)
10. [Troubleshooting](#10-troubleshooting)
11. [Best Practices](#11-best-practices)
12. [Next Steps](#12-next-steps)
13. [Glossary](#13-glossary)

---

## 1. Introduction & Overview

### What is GenZF?

GenZF is a financial portfolio management system that allows users to:
- Track trading assets (Gold, Bitcoin, Forex, stocks, etc.)
- Manage investment portfolios
- Monitor asset prices and performance
- Create budgets and financial plans

### What is Microservices Architecture?

Instead of having one big application (monolith), we split the system into **small, independent services** that work together. Think of it like a restaurant:

- **Monolith**: One person cooks, serves, cleans, and manages everything
- **Microservices**: Chef cooks, waiter serves, cleaner cleans - each person focuses on their specialty

**Benefits:**
- Each service can be updated independently
- Different teams can work on different services
- Easier to scale specific parts
- Failures in one service don't crash the entire system

### Why Use Microservices?

For GenZF, we use microservices because:

1. **Separation of Concerns**: Authentication logic is separate from business logic
2. **Security**: Sensitive auth data is isolated in its own database
3. **Scalability**: We can scale the main service independently from auth
4. **Maintainability**: Smaller codebases are easier to understand and modify

### System at a Glance

GenZF consists of **3 main services**:

```
┌─────────────────────────────────────────────────────┐
│                     CLIENT                          │
│          (Web App, Mobile App, etc.)                │
└──────────────────────┬──────────────────────────────┘
                       │
                       │ HTTP Requests
                       │ (via port 8888)
                       ▼
┌─────────────────────────────────────────────────────┐
│              API GATEWAY (Port 8888)                │
│                                                     │
│  • Single entry point for all requests             │
│  • Validates JWT tokens                            │
│  • Routes requests to correct service              │
└──────────┬─────────────────────────┬────────────────┘
           │                         │
           │                         │
           ▼                         ▼
┌──────────────────┐      ┌──────────────────────────┐
│  AUTH SERVICE    │      │    MAIN SERVICE          │
│  (Port 8080)     │      │    (Port 8081)           │
│                  │      │                          │
│  • User Login    │      │  • Asset Management      │
│  • Registration  │      │  • Portfolio Management  │
│  • JWT Tokens    │      │  • Business Logic        │
│  • Roles/Perms   │      │  • Charts & Reports      │
│                  │      │                          │
│  Database:       │      │  Database:               │
│  auth_service    │      │  genzf                   │
└──────────────────┘      └──────────────────────────┘
```

**Quick Facts:**
- **Language**: Java 21
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Tokens)
- **API Gateway**: Spring Cloud Gateway

---

## 2. System Architecture

### High-Level Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      CLIENT LAYER                            │
│  (Browser, Mobile App, Postman, etc.)                        │
└────────────────────────┬─────────────────────────────────────┘
                         │
                         │ HTTPS
                         ▼
┌──────────────────────────────────────────────────────────────┐
│                   API GATEWAY LAYER                          │
│                    (Port 8888)                               │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │  1. Receive Request                                │     │
│  │  2. Extract JWT Token                              │     │
│  │  3. Validate Token Signature                       │     │
│  │  4. Check Token Revocation (Introspect)           │     │
│  │  5. Route to Appropriate Service                   │     │
│  └────────────────────────────────────────────────────┘     │
└──────────┬───────────────────────────────┬───────────────────┘
           │                               │
           │ /auth-service/**             │ /main-service/**
           ▼                               ▼
┌─────────────────────┐         ┌──────────────────────────────┐
│  AUTH SERVICE       │         │  MAIN SERVICE                │
│  (Port 8080)        │         │  (Port 8081)                 │
│                     │         │  Context: /main-service      │
│  Controllers:       │         │                              │
│  • Authentication   │         │  Controllers:                │
│  • Users            │         │  • Assets                    │
│  • Roles            │         │  • Portfolios                │
│  • Permissions      │         │  • Charts                    │
│                     │         │                              │
│  Services:          │         │  Services:                   │
│  • Generate JWT     │         │  • Business Logic            │
│  • Validate Token   │         │  • Data Processing           │
│  • Manage Users     │         │                              │
│                     │         │                              │
│  ▼                  │         │  ▼                           │
│  PostgreSQL         │         │  PostgreSQL                  │
│  Database:          │         │  Database:                   │
│  auth_service       │         │  genzf                       │
│                     │         │                              │
│  Tables:            │         │  Tables:                     │
│  • users            │         │  • assets                    │
│  • roles            │         │  • portfolios                │
│  • permissions      │         │  • asset_users               │
│  • invalided_token  │         │  • chart_data                │
└─────────────────────┘         └──────────────────────────────┘
```

### Service Communication Flow

**Example: User wants to create a portfolio**

```
1. Client → API Gateway
   POST http://localhost:8888/main-service/portfolios
   Headers: Authorization: Bearer <JWT_TOKEN>
   Body: { "userId": "123", "name": "My Portfolio" }

2. API Gateway:
   a. Extracts JWT from Authorization header
   b. Verifies JWT signature (is it valid?)
   c. Calls Auth Service to check if token is revoked
   d. If valid, extracts user info from token
   e. Forwards request to Main Service

3. Main Service:
   a. Receives request at /main-service/portfolios
   b. Checks user permissions (@PreAuthorize)
   c. Creates portfolio in database
   d. Returns response

4. API Gateway → Client:
   Returns the portfolio data
```

### Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Language** | Java 21 | Modern Java with latest features |
| **Framework** | Spring Boot 3.x | Rapid application development |
| **API Gateway** | Spring Cloud Gateway | Reactive routing and filtering |
| **Security** | Spring Security | Authentication & Authorization |
| **Database** | PostgreSQL | Relational database for data |
| **ORM** | Spring Data JPA / Hibernate | Database object mapping |
| **JWT** | Nimbus JOSE JWT | Token generation and validation |
| **API Docs** | Springdoc OpenAPI (Swagger) | Auto-generated API documentation |
| **Build Tool** | Maven | Dependency management |
| **Testing** | JUnit 5, Spring Test | Unit and integration testing |

### Port Assignments and URLs

| Service | Port | Context Path | Direct URL | Via Gateway |
|---------|------|--------------|------------|-------------|
| **API Gateway** | 8888 | / | N/A | `http://localhost:8888` |
| **Auth Service** | 8080 | / | `http://localhost:8080` | `http://localhost:8888/auth-service` |
| **Main Service** | 8081 | /main-service | `http://localhost:8081/main-service` | `http://localhost:8888/main-service` |

**Important:** Always use the Gateway URLs in production. Direct service access is only for development/debugging.

---

## 3. Services Deep Dive

### 3.1 API Gateway (Port 8888)

#### Purpose
The API Gateway is the **single entry point** for all client requests. It acts like a gatekeeper and traffic controller.

#### Responsibilities
1. **JWT Validation**: Checks if the token is valid and not expired
2. **Token Introspection**: Asks Auth Service if token has been revoked (logged out)
3. **Request Routing**: Sends requests to the right service
4. **CORS Handling**: Manages cross-origin requests for web apps
5. **Load Balancing**: Can distribute requests across multiple service instances

#### Key Components

**File**: `server/api-gateway/src/main/resources/application.yaml`
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://localhost:8080
          predicates:
            - Path=/auth-service/**
          filters:
            - StripPrefix=1
        
        - id: main-service
          uri: http://localhost:8081
          predicates:
            - Path=/main-service/**
```

**How it works:**
- Request to `/auth-service/users` → Strips `/auth-service` → Forwards to `http://localhost:8080/users`
- Request to `/main-service/assets` → Keeps `/main-service` → Forwards to `http://localhost:8081/main-service/assets`

#### JWT Filter

**File**: `server/api-gateway/src/main/java/com/qnit18/api_gateway/security/GatewayJwtAuthenticationFilter.java`

This filter runs on **every request** and:
1. Extracts JWT from `Authorization: Bearer <token>` header
2. Decodes and validates the token
3. Calls Auth Service to check if token is revoked
4. Creates Spring Security authentication object
5. Allows request to proceed

#### Configuration

**File**: `server/api-gateway/src/main/resources/application.yaml`
```yaml
server:
  port: 8888

jwt:
  signing-key: <base64-encoded-secret>

auth-service:
  url: http://localhost:8080
```

---

### 3.2 Auth Service (Port 8080)

#### Purpose
Handles **everything related to users and authentication**.

#### Responsibilities
1. **User Registration**: Create new user accounts
2. **Login**: Verify credentials and generate JWT tokens
3. **Token Generation**: Create JWT with user info and permissions
4. **Token Validation**: Check if tokens are valid and not revoked
5. **User Management**: CRUD operations on users
6. **Role Management**: Assign roles (USER, ADMIN) to users
7. **Permission Management**: Define what each role can do

#### Key Components

##### Controllers

**1. AuthenticationController** (`server/auth-service/src/main/java/com/qnit18/auth_service/controller/AuthenticationController.java`)

```java
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    
    @PostMapping("/token")           // Login
    @PostMapping("/introspect")      // Validate token
    @PostMapping("/logout")          // Logout (revoke token)
    @PostMapping("/refresh-token")   // Get new token
}
```

**2. UserController** (`server/auth-service/src/main/java/com/qnit18/auth_service/controller/UserController.java`)

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @PostMapping                     // Create user
    @GetMapping("/{userId}")         // Get user by ID
    @GetMapping("/my-info")          // Get current user info
    @GetMapping                      // List all users (ADMIN only)
    @PutMapping("/{userId}")         // Update user
    @DeleteMapping("/{userId}")      // Delete user (ADMIN only)
}
```

**3. RoleController & PermissionController**: Manage roles and permissions

##### Services

**AuthenticationService** - Core authentication logic:
- `authenticate()`: Verify username/password, generate JWT
- `introspect()`: Check if token is valid and not revoked
- `logout()`: Add token to blacklist (invalided_token table)
- `refreshToken()`: Generate new token, invalidate old one

##### Database Schema

**Database**: `auth_service`

```sql
-- Users table
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    dob DATE
);

-- Roles table
CREATE TABLE roles (
    name VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255)
);

-- Permissions table
CREATE TABLE permissions (
    name VARCHAR(255) PRIMARY KEY,
    description VARCHAR(255)
);

-- User-Role mapping (Many-to-Many)
CREATE TABLE user_roles (
    user_id VARCHAR(255),
    role_name VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_name) REFERENCES roles(name)
);

-- Role-Permission mapping (Many-to-Many)
CREATE TABLE role_permissions (
    role_name VARCHAR(255),
    permission_name VARCHAR(255),
    FOREIGN KEY (role_name) REFERENCES roles(name),
    FOREIGN KEY (permission_name) REFERENCES permissions(name)
);

-- Revoked tokens (for logout)
CREATE TABLE invalided_token (
    id VARCHAR(255) PRIMARY KEY,
    expiry_time TIMESTAMP
);
```

##### API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/token` | No | Login and get JWT |
| POST | `/auth/introspect` | No | Validate token |
| POST | `/auth/logout` | No | Logout (revoke token) |
| POST | `/auth/refresh-token` | No | Refresh expired token |
| POST | `/users` | No | Register new user |
| GET | `/users/my-info` | Yes | Get current user info |
| GET | `/users/{id}` | Yes | Get user by ID |
| GET | `/users` | Admin | List all users |
| PUT | `/users/{id}` | Yes | Update user |
| DELETE | `/users/{id}` | Admin | Delete user |
| POST | `/roles` | Admin | Create role |
| GET | `/roles` | Yes | List all roles |
| DELETE | `/roles/{name}` | Admin | Delete role |
| POST | `/permissions` | Admin | Create permission |
| GET | `/permissions` | Yes | List all permissions |
| DELETE | `/permissions/{name}` | Admin | Delete permission |

##### Configuration

**File**: `server/auth-service/src/main/resources/application.yaml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_service
    username: postgres
    password: 123456

jwt:
  signing-key: <base64-encoded-secret>
  valid-duration: 3600        # 1 hour
  refreshable-duration: 36000 # 10 hours
```

---

### 3.3 Main Service (Port 8081)

#### Purpose
Contains the **core business logic** for the GenZF application.

#### Responsibilities
1. **Asset Management**: Track gold, bitcoin, forex, stocks, etc.
2. **Portfolio Management**: User investment portfolios
3. **Price Updates**: Update and track asset prices
4. **Reports & Charts**: Generate financial charts and analytics
5. **User Assets**: Track which assets users own

#### Key Components

##### Controllers

**1. AssetController** (`server/main-service/src/main/java/com/qnit18/main_service/controller/AssetController.java`)

```java
@RestController
@RequestMapping("/assets")
public class AssetController {
    
    @GetMapping                      // Get all assets (PUBLIC)
    @GetMapping("/{id}")             // Get asset by ID (PUBLIC)
    @GetMapping("/symbol/{symbol}")  // Get by symbol (PUBLIC)
    @GetMapping("/home")             // Get home page assets (PUBLIC)
    @GetMapping("/name")             // Get all asset names (PUBLIC)
    @PostMapping                     // Create asset (ADMIN only)
    @PutMapping("/{id}")             // Update asset (ADMIN only)
    @PutMapping("/{id}/price")       // Update price (ADMIN only)
    @DeleteMapping("/{id}")          // Delete asset (ADMIN only)
}
```

**2. PortfolioController** (`server/main-service/src/main/java/com/qnit18/main_service/controller/PortfolioController.java`)

```java
@RestController
@RequestMapping("/portfolios")
public class PortfolioController {
    
    @PostMapping                     // Create portfolio (AUTH required)
    @GetMapping("/{id}")             // Get portfolio by ID (AUTH required)
    @GetMapping("/user/{userId}")    // Get user's portfolio (AUTH required)
    @DeleteMapping("/{id}")          // Delete portfolio (AUTH required)
}
```

##### Database Schema

**Database**: `genzf`

```sql
-- Assets table
CREATE TABLE assets (
    id UUID PRIMARY KEY,
    symbol VARCHAR(255) UNIQUE NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,  -- FOREX, CRYPTO, COMMODITIES, STOCKS
    current_price DECIMAL(19,4) NOT NULL,
    change_percentage FLOAT NOT NULL,
    change_value DECIMAL(19,4) NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    currency VARCHAR(3) NOT NULL,
    open DECIMAL(19,4) NOT NULL,
    high DECIMAL(19,4) NOT NULL,
    low DECIMAL(19,4) NOT NULL,
    volume BIGINT NOT NULL,
    market_status VARCHAR(50) NOT NULL,  -- OPEN, CLOSED
    asset_home BOOLEAN NOT NULL DEFAULT false
);

-- Portfolios table
CREATE TABLE portfolios (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP
);

-- Asset-User relationship
CREATE TABLE asset_users (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    asset_id UUID NOT NULL,
    quantity DECIMAL(19,4),
    FOREIGN KEY (asset_id) REFERENCES assets(id)
);
```

##### API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/main-service/assets` | No | List all assets with pagination |
| GET | `/main-service/assets/{id}` | No | Get asset by ID |
| GET | `/main-service/assets/symbol/{symbol}` | No | Get asset by symbol |
| GET | `/main-service/assets/home` | No | Get home page assets |
| GET | `/main-service/assets/name` | No | Get all asset names |
| POST | `/main-service/assets` | Admin | Create new asset |
| PUT | `/main-service/assets/{id}` | Admin | Update asset |
| PUT | `/main-service/assets/{id}/price` | Admin | Update asset price |
| DELETE | `/main-service/assets/{id}` | Admin | Delete asset |
| POST | `/main-service/portfolios` | Yes | Create portfolio |
| GET | `/main-service/portfolios/{id}` | Yes | Get portfolio |
| GET | `/main-service/portfolios/user/{userId}` | Yes | Get user's portfolio |
| DELETE | `/main-service/portfolios/{id}` | Yes | Delete portfolio |

##### Configuration

**File**: `server/main-service/src/main/resources/application.yaml`

```yaml
server:
  port: 8081
  servlet:
    context-path: /main-service

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/genzf
    username: postgres
    password: 123456

jwt:
  signing-key: <same-as-auth-service>

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

**Swagger UI**: `http://localhost:8081/main-service/swagger-ui.html`

---

## 4. Key Concepts Explained

### 4.1 JWT (JSON Web Tokens)

#### What is JWT?

Think of JWT like a **digital passport**. When you log in, the server gives you this passport. Every time you make a request, you show your passport to prove who you are.

#### JWT Structure

A JWT has 3 parts separated by dots:

```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huIn0.signature
└─────────────────┘ └─────────────┘ └─────────┘
      HEADER           PAYLOAD       SIGNATURE
```

**1. Header**: Tells us the algorithm used (HS512)
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

**2. Payload (Claims)**: Contains user information
```json
{
  "sub": "john",                    // Username
  "userId": "123-456-789",          // User ID
  "iss": "qnit18.com",              // Issuer
  "iat": 1705245600,                // Issued at (timestamp)
  "exp": 1705249200,                // Expires at (timestamp)
  "jti": "unique-token-id",         // JWT ID
  "scope": "ROLE_USER CREATE_DATA"  // Roles and permissions
}
```

**3. Signature**: Proves the token wasn't tampered with
```
HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret-key
)
```

#### How JWT Works in GenZF

```
1. User logs in with username/password
   ↓
2. Auth Service verifies credentials
   ↓
3. Auth Service generates JWT with user info
   ↓
4. Client stores JWT (usually in browser)
   ↓
5. Client includes JWT in every request:
   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
   ↓
6. API Gateway validates JWT
   ↓
7. Request is forwarded to service if valid
```

#### Why Use JWT?

- **Stateless**: Server doesn't need to store session info
- **Scalable**: Works across multiple servers
- **Secure**: Can't be modified without detection
- **Self-contained**: Contains all user info needed

### 4.2 API Gateway Pattern

#### Why Do We Need an API Gateway?

**Without API Gateway:**
```
Client → Auth Service (port 8080)
Client → Main Service (port 8081)
Client → Another Service (port 8082)
Client → Yet Another Service (port 8083)
```

Problems:
- Client needs to know all service URLs
- Each service handles authentication separately
- Hard to add logging, monitoring, rate limiting
- CORS configuration in every service

**With API Gateway:**
```
Client → API Gateway (port 8888)
         ↓
         ├→ Auth Service
         ├→ Main Service  
         ├→ Another Service
         └→ Yet Another Service
```

Benefits:
- **Single Entry Point**: Client only knows one URL
- **Centralized Authentication**: JWT validation in one place
- **Easy Monitoring**: All requests go through gateway
- **Simplified Client**: No need to know about services
- **Load Balancing**: Gateway can distribute requests
- **Versioning**: Can route `/v1/` and `/v2/` to different services

### 4.3 Authentication vs Authorization

Many beginners confuse these!

#### Authentication: "Who are you?"
Proving your identity (like showing your ID at airport)

**In GenZF:**
```java
// Login endpoint - Authenticates the user
@PostMapping("/auth/token")
public JwtToken login(String username, String password) {
    // Verify username and password match
    // If yes, generate JWT token
}
```

#### Authorization: "What can you do?"
Checking permissions after identity is confirmed (like checking if you have VIP ticket)

**In GenZF:**
```java
// Authorization check - User must be ADMIN
@DeleteMapping("/assets/{id}")
@PreAuthorize("hasRole('ADMIN')")  // ← This is authorization
public void deleteAsset(UUID id) {
    // Only ADMINs can reach this code
}
```

#### Example Flow

```
1. User logs in (username: john, password: secret123)
   → Authentication ✓
   → Token issued with role: USER

2. John tries to view assets
   GET /assets
   → Has valid token ✓ (authenticated)
   → Endpoint is public ✓ (no authorization needed)
   → SUCCESS

3. John tries to delete an asset
   DELETE /assets/123
   → Has valid token ✓ (authenticated)
   → Has ADMIN role? ✗ (authorization failed)
   → FORBIDDEN 403

4. Admin logs in (username: admin, password: admin123)
   → Authentication ✓
   → Token issued with role: ADMIN

5. Admin tries to delete an asset
   DELETE /assets/123
   → Has valid token ✓ (authenticated)
   → Has ADMIN role? ✓ (authorized)
   → SUCCESS
```

### 4.4 Microservices Communication

#### Synchronous Communication (HTTP)

Services talk to each other using REST APIs.

**Example:** Gateway → Auth Service

```java
// In API Gateway
@Component
public class AuthServiceClient {
    
    @Autowired
    private WebClient.Builder webClient;
    
    public IntrospectResponse introspectToken(String token) {
        return webClient
            .build()
            .post()
            .uri("http://localhost:8080/auth/introspect")
            .bodyValue(new IntrospectRequest(token))
            .retrieve()
            .bodyToMono(IntrospectResponse.class)
            .block();  // Waits for response
    }
}
```

**Pros:**
- Simple to understand
- Immediate response
- Easy to debug

**Cons:**
- Services are coupled (one waits for the other)
- If Auth Service is down, Gateway fails

#### Asynchronous Communication (Message Queue)

Services send messages to a queue, don't wait for response (not currently used in GenZF, but good to know).

### 4.5 Database Per Service Pattern

Each service has its **own database**. Services don't share databases.

```
Auth Service  →  auth_service database
Main Service  →  genzf database
```

#### Why?

1. **Independence**: Can change database without affecting other services
2. **Scalability**: Can scale databases independently
3. **Security**: Auth database is isolated
4. **Technology Freedom**: Could use PostgreSQL for one, MongoDB for another

#### How Services Share Data?

Through **APIs**, not direct database access!

**Wrong:**
```
Main Service → Queries auth_service database directly ✗
```

**Correct:**
```
Main Service → Calls Auth Service API → Auth Service queries its database ✓
```

### 4.6 Spring Boot Basics

#### What is Spring Boot?

Spring Boot is a framework that makes creating Java applications **super easy**. It provides:

- **Auto-configuration**: Automatically sets up common things
- **Embedded Server**: No need to install Tomcat separately
- **Starter Dependencies**: One dependency brings in many related libraries
- **Production-Ready**: Built-in monitoring, health checks, etc.

#### Key Annotations

```java
// Marks this class as a REST controller
@RestController
public class UserController {
    
    // Injects UserService automatically
    @Autowired
    private UserService userService;
    
    // Maps GET requests to /users
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
    
    // Maps POST requests to /users
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
    
    // {id} is a path variable
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable String id) {
        return userService.findById(id);
    }
}
```

#### Spring Boot Application Structure

```java
@SpringBootApplication  // ← This is the magic annotation
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        // Starts embedded Tomcat server
        // Scans for @Controller, @Service, @Repository
        // Sets up Spring Security, JPA, etc.
    }
}
```

### 4.7 Spring Security Basics

Spring Security handles authentication and authorization in Spring Boot apps.

#### Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf().disable()  // Disable CSRF for APIs
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication)
                .requestMatchers("/auth/token").permitAll()
                .requestMatchers("/users").permitAll()
                
                // Protected endpoints (need authentication)
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> 
                oauth2.jwt()  // Use JWT for authentication
            );
        
        return http.build();
    }
}
```

#### Method-Level Security

```java
@RestController
public class AssetController {
    
    // Anyone can access (even without login)
    @GetMapping("/assets")
    public List<Asset> getAssets() { }
    
    // Must be authenticated (any logged-in user)
    @PostMapping("/portfolios")
    @PreAuthorize("isAuthenticated()")
    public Portfolio create() { }
    
    // Must have ADMIN role
    @DeleteMapping("/assets/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(UUID id) { }
    
    // Must be ADMIN OR the owner
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    public User update(@PathVariable String id) { }
}
```

---

## 5. Request Flow Examples

### 5.1 User Registration Flow

Let's walk through creating a new user account step by step.

#### Step-by-Step Flow

```
┌────────┐                ┌─────────┐              ┌──────────┐
│ Client │                │ Gateway │              │   Auth   │
│        │                │  (8888) │              │ Service  │
└───┬────┘                └────┬────┘              └────┬─────┘
    │                          │                        │
    │ POST /auth-service/users │                        │
    │ Body: {username, pass}   │                        │
    ├─────────────────────────>│                        │
    │                          │                        │
    │                          │ Route to /users        │
    │                          ├───────────────────────>│
    │                          │                        │
    │                          │                        │ 1. Validate input
    │                          │                        │ 2. Hash password
    │                          │                        │ 3. Save to DB
    │                          │                        │ 4. Assign USER role
    │                          │                        │
    │                          │ User created           │
    │                          │<───────────────────────┤
    │                          │                        │
    │ 201 Created              │                        │
    │ User details             │                        │
    │<─────────────────────────┤                        │
    │                          │                        │
```

#### Actual Request

```bash
curl -X POST http://localhost:8888/auth-service/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "secret123",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01"
  }'
```

#### Response

```json
{
  "code": 1000,
  "result": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01",
    "roles": ["USER"]
  }
}
```

#### What Happens in Code

**1. Controller receives request** (`UserController.java`)
```java
@PostMapping
ApiBaseResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
    // Validation happens automatically via @Valid
    return ApiBaseResponse.<UserResponse>builder()
        .result(userService.createUser(request))
        .build();
}
```

**2. Service processes logic** (`UserService.java`)
```java
public UserResponse createUser(UserCreationRequest request) {
    // Check if username exists
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new AppException(ErrorCode.USER_EXISTED);
    }
    
    // Hash password
    String hashedPassword = passwordEncoder.encode(request.getPassword());
    
    // Create user entity
    User user = userMapper.toUser(request);
    user.setPassword(hashedPassword);
    
    // Assign default USER role
    Role userRole = roleRepository.findById("USER")
        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
    user.setRoles(Set.of(userRole));
    
    // Save to database
    user = userRepository.save(user);
    
    return userMapper.toUserResponse(user);
}
```

**3. Password is hashed** (Security!)
```
Original: "secret123"
Hashed:   "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhW"
```

### 5.2 Login Flow

User logging in and getting a JWT token.

#### Step-by-Step Flow

```
┌────────┐          ┌─────────┐           ┌──────────┐           ┌──────────┐
│ Client │          │ Gateway │           │   Auth   │           │ Database │
│        │          │  (8888) │           │ Service  │           │          │
└───┬────┘          └────┬────┘           └────┬─────┘           └────┬─────┘
    │                    │                     │                      │
    │ POST /auth/token   │                     │                      │
    │ {username, pass}   │                     │                      │
    ├───────────────────>│                     │                      │
    │                    │                     │                      │
    │                    │ Route to /auth/token│                      │
    │                    ├────────────────────>│                      │
    │                    │                     │                      │
    │                    │                     │ 1. Find user by username
    │                    │                     ├─────────────────────>│
    │                    │                     │                      │
    │                    │                     │<─────────────────────┤
    │                    │                     │ User found           │
    │                    │                     │                      │
    │                    │                     │ 2. Verify password   │
    │                    │                     │    (compare hash)    │
    │                    │                     │                      │
    │                    │                     │ 3. Generate JWT      │
    │                    │                     │    - Add username    │
    │                    │                     │    - Add roles       │
    │                    │                     │    - Add expiry      │
    │                    │                     │    - Sign with key   │
    │                    │                     │                      │
    │                    │ JWT token          │                      │
    │                    │<────────────────────┤                      │
    │                    │                     │                      │
    │ 200 OK             │                     │                      │
    │ {token: "eyJ..."}  │                     │                      │
    │<───────────────────┤                     │                      │
    │                    │                     │                      │
    │ SAVE TOKEN         │                     │                      │
    │ (browser/storage)  │                     │                      │
    │                    │                     │                      │
```

#### Actual Request

```bash
curl -X POST http://localhost:8888/auth-service/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "secret123"
  }'
```

#### Response

```json
{
  "code": 1000,
  "result": {
    "authenticated": true,
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huIiwic2NvcGUiOiJST0xFX1VTRVIiLCJpc3MiOiJxbml0MTguY29tIiwiZXhwIjoxNzA1MjQ5MjAwLCJpYXQiOjE3MDUyNDU2MDAsImp0aSI6IjU1MGU4NDAwLWUyOWItNDFkNC1hNzE2LTQ0NjY1NTQ0MDAwMCIsInVzZXJJZCI6IjU1MGU4NDAwLWUyOWItNDFkNC1hNzE2LTQ0NjY1NTQ0MDAwMCJ9.vXR5ZB8K9ks..."
  }
}
```

#### What Happens in Code

**1. Controller receives login request**
```java
@PostMapping("/token")
public ApiBaseResponse<AuthenticationResponse> login(
    @RequestBody AuthenticationRequest request
) {
    var result = authenticationService.authenticate(request);
    return ApiBaseResponse.<AuthenticationResponse>builder()
        .result(result)
        .build();
}
```

**2. Service authenticates user**
```java
public AuthenticationResponse authenticate(AuthenticationRequest request) {
    // 1. Find user by username
    User user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    
    // 2. Verify password
    boolean authenticated = passwordEncoder.matches(
        request.getPassword(),  // Plain text from request
        user.getPassword()      // Hashed password from DB
    );
    
    if (!authenticated) {
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
    
    // 3. Generate JWT token
    String token = generateToken(user);
    
    return AuthenticationResponse.builder()
        .authenticated(true)
        .token(token)
        .build();
}
```

**3. Token generation**
```java
private String generateToken(User user) {
    // Build scope string: "ROLE_USER ROLE_ADMIN CREATE_DATA"
    String scope = user.getRoles().stream()
        .flatMap(role -> Stream.concat(
            Stream.of("ROLE_" + role.getName()),
            role.getPermissions().stream().map(Permission::getName)
        ))
        .collect(Collectors.joining(" "));
    
    // Create JWT
    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(user.getUsername())              // Username
        .issuer("qnit18.com")                     // Who issued it
        .issueTime(new Date())                    // When issued
        .expirationTime(new Date(
            Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
        ))                                         // When expires (1 hour)
        .jwtID(UUID.randomUUID().toString())      // Unique ID
        .claim("scope", scope)                    // Roles & permissions
        .claim("userId", user.getId())            // User ID for business logic
        .build();
    
    // Sign with secret key
    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader(JWSAlgorithm.HS512),
        claimsSet
    );
    
    signedJWT.sign(new MACSigner(signingKey.getBytes()));
    
    return signedJWT.serialize();
}
```

### 5.3 Protected Endpoint Access Flow

User accessing a protected endpoint with JWT token.

#### Step-by-Step Flow

```
┌────────┐        ┌─────────┐         ┌──────────┐        ┌──────────┐
│ Client │        │ Gateway │         │   Auth   │        │   Main   │
│        │        │  (8888) │         │ Service  │        │ Service  │
└───┬────┘        └────┬────┘         └────┬─────┘        └────┬─────┘
    │                  │                   │                   │
    │ GET /main-service/portfolios/user/123                    │
    │ Authorization: Bearer eyJ...          │                  │
    ├─────────────────>│                   │                  │
    │                  │                   │                  │
    │                  │ 1. Extract JWT    │                  │
    │                  │                   │                  │
    │                  │ 2. Decode JWT     │                  │
    │                  │    Verify signature│                 │
    │                  │                   │                  │
    │                  │ 3. Check expiry   │                  │
    │                  │    (not expired?) │                  │
    │                  │                   │                  │
    │                  │ 4. Introspect     │                  │
    │                  │ POST /auth/introspect                │
    │                  ├──────────────────>│                  │
    │                  │ {token: "eyJ..."}│                  │
    │                  │                   │                  │
    │                  │                   │ Check if revoked │
    │                  │                   │ (in blacklist?) │
    │                  │                   │                  │
    │                  │ {valid: true}     │                  │
    │                  │<──────────────────┤                  │
    │                  │                   │                  │
    │                  │ 5. Extract user info from JWT        │
    │                  │    (username, roles, userId)         │
    │                  │                   │                  │
    │                  │ 6. Forward request with JWT          │
    │                  │                   │                  │
    │                  │ GET /main-service/portfolios/user/123│
    │                  │ Authorization: Bearer eyJ...         │
    │                  ├─────────────────────────────────────>│
    │                  │                   │                  │
    │                  │                   │                  │ 7. Decode JWT
    │                  │                   │                  │ 8. Check @PreAuthorize
    │                  │                   │                  │ 9. Execute business logic
    │                  │                   │                  │
    │                  │ Portfolio data    │                  │
    │                  │<─────────────────────────────────────┤
    │                  │                   │                  │
    │ 200 OK           │                   │                  │
    │ Portfolio data   │                   │                  │
    │<─────────────────┤                   │                  │
    │                  │                   │                  │
```

#### Actual Request

```bash
curl -X GET http://localhost:8888/main-service/portfolios/user/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWI..."
```

#### Response

```json
{
  "code": 1000,
  "result": {
    "id": "portfolio-id-here",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "My Investment Portfolio",
    "netWorth": 15000.00,
    "assets": [...]
  }
}
```

### 5.4 Token Refresh Flow

When token expires, get a new one without logging in again.

#### When to Refresh?

- **Valid duration**: 1 hour (3600 seconds)
- **Refreshable duration**: 10 hours (36000 seconds)

**Timeline:**
```
Login at 10:00 AM
├─ Token valid: 10:00 AM - 11:00 AM (can use normally)
├─ Token expired but refreshable: 11:00 AM - 8:00 PM (can refresh)
└─ Token not refreshable: After 8:00 PM (must login again)
```

#### Request

```bash
curl -X POST http://localhost:8888/auth-service/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGc... (expired token)"
  }'
```

#### Response

```json
{
  "code": 1000,
  "result": {
    "authenticated": true,
    "token": "eyJhbGc... (new token)"
  }
}
```

#### What Happens

1. Auth Service checks if token is within refreshable duration
2. If yes, marks old token as invalid (adds to blacklist)
3. Generates new token with fresh expiration
4. Returns new token

### 5.5 Logout Flow

Revoke the token so it can't be used anymore.

#### Request

```bash
curl -X POST http://localhost:8888/auth-service/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGc..."
  }'
```

#### What Happens

1. Auth Service extracts JWT ID (`jti` claim) from token
2. Adds token to `invalided_token` table (blacklist)
3. Token is now invalid, even if not expired
4. Future requests with this token will be rejected

#### In Database

```sql
-- Before logout
SELECT * FROM invalided_token;
(empty)

-- After logout
SELECT * FROM invalided_token;
id                                    | expiry_time
--------------------------------------+---------------------
550e8400-e29b-41d4-a716-446655440000 | 2024-01-14 11:00:00
```

---

## 6. Project Structure

### 6.1 Overall Structure

```
server/
├── api-gateway/           # API Gateway service
├── auth-service/          # Authentication service
├── main-service/          # Main business logic service
├── ARCHITECTURE.md        # Architecture documentation
├── QUICK_START.md         # Quick start guide
├── IMPLEMENTATION_SUMMARY.md  # Implementation summary
└── BEGINNER_GUIDE.md      # This file!
```

### 6.2 Service Structure (Example: auth-service)

```
auth-service/
├── pom.xml                              # Maven dependencies
├── mvnw / mvnw.cmd                      # Maven wrapper
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/qnit18/auth_service/
│   │   │       ├── AuthServiceApplication.java   # Main class
│   │   │       ├── configuration/               # Configuration classes
│   │   │       │   ├── ApplicationInitConfig.java
│   │   │       │   ├── JwtDecoderConfig.java
│   │   │       │   ├── PasswordEncoderConfig.java
│   │   │       │   └── SecurityConfig.java
│   │   │       ├── constant/                    # Constants
│   │   │       │   └── PredefinedRole.java
│   │   │       ├── controller/                  # REST Controllers
│   │   │       │   ├── AuthenticationController.java
│   │   │       │   ├── UserController.java
│   │   │       │   ├── RoleController.java
│   │   │       │   └── PermissionController.java
│   │   │       ├── dto/                         # Data Transfer Objects
│   │   │       │   ├── request/                 # Request DTOs
│   │   │       │   └── response/                # Response DTOs
│   │   │       ├── entity/                      # Database entities
│   │   │       │   ├── User.java
│   │   │       │   ├── Role.java
│   │   │       │   ├── Permission.java
│   │   │       │   └── InvalidedToken.java
│   │   │       ├── exception/                   # Exception handling
│   │   │       │   ├── AppException.java
│   │   │       │   ├── ErrorCode.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       ├── mapper/                      # Entity<->DTO mappers
│   │   │       │   ├── UserMapper.java
│   │   │       │   ├── RoleMapper.java
│   │   │       │   └── PermissionMapper.java
│   │   │       ├── repository/                  # Database repositories
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── RoleRepository.java
│   │   │       │   ├── PermissionRepository.java
│   │   │       │   └── InvalidedTokenRepository.java
│   │   │       ├── service/                     # Business logic
│   │   │       │   ├── AuthenticationService.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── RoleService.java
│   │   │       │   └── PermissionService.java
│   │   │       └── validator/                   # Custom validators
│   │   └── resources/
│   │       └── application.yaml                 # Configuration file
│   └── test/
│       └── java/                               # Unit tests
└── target/                                     # Compiled code (generated)
```

### 6.3 Code Organization Layers

GenZF follows a **layered architecture**:

```
┌─────────────────────────────────────────┐
│           CONTROLLER LAYER              │  ← REST endpoints
│  (Handles HTTP requests/responses)      │
└───────────────┬─────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────┐
│            SERVICE LAYER                │  ← Business logic
│  (Processes data, applies rules)        │
└───────────────┬─────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────┐
│          REPOSITORY LAYER               │  ← Database access
│  (Queries database)                     │
└───────────────┬─────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────┐
│             DATABASE                    │
│  (PostgreSQL)                           │
└─────────────────────────────────────────┘
```

**Example Flow:**
```
1. Client → POST /users
2. UserController receives request
3. UserController → UserService.createUser()
4. UserService validates and processes
5. UserService → UserRepository.save()
6. UserRepository → Database INSERT
7. Database returns saved user
8. UserRepository → UserService → UserController → Client
```

### 6.4 Key File Types

#### Controllers
```java
@RestController              // Marks as REST controller
@RequestMapping("/users")    // Base path for all endpoints
public class UserController {
    // Handle HTTP requests
    // Call services
    // Return responses
}
```

#### Services
```java
@Service                     // Marks as service bean
@RequiredArgsConstructor     // Auto-inject dependencies
public class UserService {
    // Business logic
    // Validation
    // Call repositories
}
```

#### Repositories
```java
@Repository                  // Marks as repository bean
public interface UserRepository extends JpaRepository<User, String> {
    // Database queries
    // Spring Data JPA generates implementations automatically!
    
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

#### Entities
```java
@Entity                      // JPA entity (maps to database table)
@Table(name = "users")
public class User {
    @Id                      // Primary key
    @GeneratedValue          // Auto-generated
    private String id;
    
    @Column(unique = true)   // Unique constraint
    private String username;
    
    // ... other fields
}
```

#### DTOs (Data Transfer Objects)
```java
// Request DTO (from client)
public class UserCreationRequest {
    @NotBlank(message = "USERNAME_INVALID")
    private String username;
    
    @Size(min = 8, message = "PASSWORD_INVALID")
    private String password;
}

// Response DTO (to client)
public class UserResponse {
    private String id;
    private String username;
    // No password! (security)
}
```

### 6.5 Where to Find Things

| What you want | Where to look |
|---------------|---------------|
| **REST endpoints** | `controller/` folder |
| **Business logic** | `service/` folder |
| **Database queries** | `repository/` folder |
| **Database tables** | `entity/` folder |
| **Configuration** | `resources/application.yaml` |
| **Security settings** | `configuration/SecurityConfig.java` |
| **Error handling** | `exception/GlobalExceptionHandler.java` |
| **Error codes** | `exception/ErrorCode.java` |
| **JWT logic** | `service/AuthenticationService.java` |
| **Validation rules** | `dto/request/` classes |
| **API documentation** | Swagger UI (when service runs) |

---

## 7. Setup & Installation

### 7.1 Prerequisites

Before you start, you need to install:

1. **Java Development Kit (JDK) 21**
   - Download: https://adoptium.net/
   - Verify: `java -version` (should show 21.x.x)

2. **Maven 3.x**
   - Download: https://maven.apache.org/download.cgi
   - Or use included `mvnw` wrapper
   - Verify: `mvn -version`

3. **PostgreSQL Database**
   - Download: https://www.postgresql.org/download/
   - Install with default settings
   - Remember your password!

4. **Git**
   - Download: https://git-scm.com/downloads
   - For cloning the repository

5. **IDE (Optional but recommended)**
   - IntelliJ IDEA Community Edition (recommended)
   - VS Code with Java extensions
   - Eclipse

### 7.2 Database Setup

#### Step 1: Start PostgreSQL

Windows:
```bash
# PostgreSQL should start automatically as a service
# Or start it from Services app
```

Linux/Mac:
```bash
sudo systemctl start postgresql
# or
brew services start postgresql
```

#### Step 2: Create Databases

Open PostgreSQL command line or pgAdmin:

```sql
-- Create auth service database
CREATE DATABASE auth_service;

-- Create main service database
CREATE DATABASE genzf;

-- Verify
\list  -- (or \l)
```

You should see both databases listed.

#### Step 3: Create Database User (Optional)

By default, GenZF uses:
- Username: `postgres`
- Password: `123456`

**For production, create a dedicated user:**

```sql
CREATE USER genzf_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE auth_service TO genzf_user;
GRANT ALL PRIVILEGES ON DATABASE genzf TO genzf_user;
```

Then update `application.yaml` in each service:
```yaml
spring:
  datasource:
    username: genzf_user
    password: your_secure_password
```

### 7.3 Clone the Repository

```bash
git clone <repository-url>
cd genZF/server
```

### 7.4 Configuration

Each service has an `application.yaml` file. Check these settings:

#### Auth Service
**File**: `server/auth-service/src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_service
    username: postgres
    password: 123456  # ← Update if you changed it
```

#### Main Service
**File**: `server/main-service/src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/genzf
    username: postgres
    password: 123456  # ← Update if you changed it
```

**Important:** All services must use the **same JWT signing key**!

### 7.5 Build the Project

From `server/` directory:

```bash
# Build all services at once
mvn clean install
```

This will:
1. Download all dependencies
2. Compile the code
3. Run tests
4. Create JAR files in `target/` folders

**Troubleshooting:**
- If tests fail, skip them: `mvn clean install -DskipTests`
- If download is slow, consider using a Maven mirror

### 7.6 Running Services

**Important:** Run services in this order!

#### Option 1: Using Maven (Development)

**Terminal 1 - Auth Service:**
```bash
cd server/auth-service
mvn spring-boot:run
```

Wait for:
```
Started AuthServiceApplication in X.XXX seconds
```

**Terminal 2 - Main Service:**
```bash
cd server/main-service
mvn spring-boot:run
```

Wait for:
```
Started MainServiceApplication in X.XXX seconds
```

**Terminal 3 - API Gateway:**
```bash
cd server/api-gateway
mvn spring-boot:run
```

Wait for:
```
Started ApiGatewayApplication in X.XXX seconds
```

#### Option 2: Using JAR Files (Production-like)

After `mvn clean install`:

**Terminal 1:**
```bash
java -jar server/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar
```

**Terminal 2:**
```bash
java -jar server/main-service/target/main-service-0.0.1-SNAPSHOT.jar
```

**Terminal 3:**
```bash
java -jar server/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
```

### 7.7 Verify Installation

#### Check Service Health

**Auth Service:**
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

**Main Service:**
```bash
curl http://localhost:8081/main-service/actuator/health
```

**API Gateway:**
```bash
curl http://localhost:8888/actuator/health
```

#### Check Database Tables

Connect to PostgreSQL:
```bash
psql -U postgres
```

```sql
-- Connect to auth_service database
\c auth_service

-- List tables
\dt

-- You should see: users, roles, permissions, user_roles, role_permissions, invalided_token
```

#### Create Admin User

If tables are empty, you need to create initial admin user:

```bash
curl -X POST http://localhost:8888/auth-service/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin",
    "firstName": "Admin",
    "lastName": "User",
    "dob": "1990-01-01"
  }'
```

Then promote to admin role (manually in database for first user):
```sql
-- Connect to auth_service database
\c auth_service

-- Find user ID
SELECT id, username FROM users WHERE username = 'admin';

-- Insert ADMIN role if not exists
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrator');

-- Assign ADMIN role to user
INSERT INTO user_roles (user_id, role_name) 
VALUES ('<user_id_from_above>', 'ADMIN');
```

### 7.8 Access Swagger UI

Once Main Service is running:

**URL:** http://localhost:8081/main-service/swagger-ui.html

This shows interactive API documentation where you can:
- See all endpoints
- View request/response schemas
- Test endpoints directly

---

## 8. Development Workflow

### 8.1 How to Add a New Endpoint

Let's add a new endpoint: **GET /users/{userId}/portfolios** (get all portfolios for a user)

#### Step 1: Add Repository Method

**File**: `main-service/src/main/java/com/qnit18/main_service/repository/PortfolioRepository.java`

```java
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    
    // Add this method
    List<Portfolio> findByUserId(String userId);
}
```

Spring Data JPA automatically implements this based on method name!

#### Step 2: Add Service Method

**File**: `main-service/src/main/java/com/qnit18/main_service/service/PortfolioService.java`

```java
@Service
@RequiredArgsConstructor
public class PortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;
    
    // Add this method
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getAllPortfoliosByUserId(String userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);
        return portfolios.stream()
            .map(portfolioMapper::toPortfolioResponse)
            .collect(Collectors.toList());
    }
}
```

#### Step 3: Add Controller Endpoint

**File**: `main-service/src/main/java/com/qnit18/main_service/controller/PortfolioController.java`

```java
@RestController
@RequestMapping("/portfolios")
public class PortfolioController {
    
    private final PortfolioService portfolioService;
    
    // Add this endpoint
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ApiBaseResponse<List<PortfolioResponse>> getUserPortfolios(
        @PathVariable String userId
    ) {
        return ApiBaseResponse.<List<PortfolioResponse>>builder()
            .result(portfolioService.getAllPortfoliosByUserId(userId))
            .build();
    }
}
```

#### Step 4: Test the Endpoint

```bash
# Login first to get token
TOKEN=$(curl -X POST http://localhost:8888/auth-service/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' \
  | jq -r '.result.token')

# Test new endpoint
curl -X GET http://localhost:8888/main-service/portfolios/user/USER_ID_HERE \
  -H "Authorization: Bearer $TOKEN"
```

#### Step 5: Add to Bruno Collection

Create file: `postman/main-service/portfolios/05-get-user-portfolios.bru`

```bru
meta {
  name: Get User Portfolios
  type: http
  seq: 5
}

get {
  url: {{api-gateway-MainSERVICE}}/portfolios/user/{{sampleUserId}}
  body: none
  auth: inherit
}

settings {
  encodeUrl: true
  timeout: 0
}
```

#### Step 6: Document in Swagger

```java
@GetMapping("/user/{userId}")
@PreAuthorize("isAuthenticated()")
@Operation(
    summary = "Get all portfolios for a user",
    description = "Retrieves all portfolios owned by the specified user"
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "User not found")
})
public ApiBaseResponse<List<PortfolioResponse>> getUserPortfolios(
    @PathVariable @Parameter(description = "User ID") String userId
) {
    // ... implementation
}
```

### 8.2 How to Add a New Service

Let's say you want to add a **Notification Service**.

#### Step 1: Create Service Structure

```bash
cd server
mkdir notification-service
cd notification-service
```

#### Step 2: Create pom.xml

Copy from auth-service and modify:
```xml
<artifactId>notification-service</artifactId>
<name>notification-service</name>
```

#### Step 3: Create Application Class

```java
@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
```

#### Step 4: Add to API Gateway

**File**: `api-gateway/src/main/resources/application.yaml`

```yaml
spring:
  cloud:
    gateway:
      routes:
        # ... existing routes ...
        
        # Notification Service Route
        - id: notification-service
          uri: http://localhost:8082
          predicates:
            - Path=/notification-service/**
          filters:
            - StripPrefix=1
```

#### Step 5: Configure Security

Follow same pattern as auth-service or main-service.

### 8.3 Testing Strategies

#### Unit Tests

Test individual components in isolation:

```java
@SpringBootTest
class UserServiceTest {
    
    @MockBean
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Test
    void createUser_success() {
        // Given
        UserCreationRequest request = new UserCreationRequest();
        request.setUsername("john");
        request.setPassword("password123");
        
        // When
        UserResponse response = userService.createUser(request);
        
        // Then
        assertThat(response.getUsername()).isEqualTo("john");
        verify(userRepository).save(any(User.class));
    }
}
```

#### Integration Tests

Test full request-response cycle:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void createUser_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"john\",\"password\":\"pass123\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result.username").value("john"));
    }
}
```

#### Manual Testing with Bruno

1. Use Bruno collection in `postman/` folder
2. Select `genF` environment
3. Run requests in order

### 8.4 Debugging Tips

#### Enable Debug Logging

**File**: `application.yaml`
```yaml
logging:
  level:
    com.qnit18: DEBUG           # Your application
    org.springframework: DEBUG   # Spring framework
    org.hibernate.SQL: DEBUG     # SQL queries
```

#### View SQL Queries

```yaml
spring:
  jpa:
    show-sql: true               # Show SQL in console
    properties:
      hibernate:
        format_sql: true         # Pretty print SQL
```

#### Use IntelliJ Debugger

1. Set breakpoint by clicking left of line number
2. Run service in Debug mode (Shift+F9)
3. Make request to trigger breakpoint
4. Step through code with F8

#### Check Logs

Each service prints logs to console. Look for:
- `ERROR` - Something went wrong
- `WARN` - Potential issue
- `INFO` - Normal operations
- `DEBUG` - Detailed information

---

## 9. Common Tasks

### 9.1 Creating a New User

```bash
curl -X POST http://localhost:8888/auth-service/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "secure123",
    "firstName": "New",
    "lastName": "User",
    "dob": "1995-06-15"
  }'
```

### 9.2 Logging In

```bash
curl -X POST http://localhost:8888/auth-service/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "secure123"
  }'
```

Save the token from response!

### 9.3 Making Authenticated Requests

```bash
# Save token to variable
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# Use in requests
curl -X GET http://localhost:8888/auth-service/users/my-info \
  -H "Authorization: Bearer $TOKEN"
```

### 9.4 Creating a Portfolio

```bash
curl -X POST http://localhost:8888/main-service/portfolios \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "your-user-id",
    "name": "My Investment Portfolio"
  }'
```

### 9.5 Viewing All Assets

```bash
# Public endpoint - no auth needed
curl http://localhost:8888/main-service/assets
```

### 9.6 Testing with Bruno

1. Open Bruno app
2. Open collection: `File` → `Open Collection` → Select `postman/` folder
3. Select `genF` environment (top right)
4. Run `auth-service/authentication/01-login.bru`
5. Copy token from response
6. Paste into `environments/genF.bru`:
   ```
   token: eyJhbGciOiJIUzUxMiJ9...
   ```
7. Now run any authenticated endpoint!

---

## 10. Troubleshooting

### 10.1 Common Errors and Solutions

#### Error: "Connection Refused" or "Service Unavailable"

**Symptom:**
```
curl: (7) Failed to connect to localhost port 8080: Connection refused
```

**Cause:** Service is not running

**Solution:**
1. Check if service is running: `netstat -an | grep 8080`
2. Start the service: `cd auth-service && mvn spring-boot:run`
3. Check logs for errors

#### Error: "401 Unauthorized"

**Symptom:**
```json
{
  "code": 401,
  "message": "Unauthenticated"
}
```

**Causes & Solutions:**

1. **No token provided**
   - Add `Authorization: Bearer <token>` header

2. **Token expired**
   - Token valid for 1 hour only
   - Login again to get new token
   - Or use refresh token endpoint

3. **Invalid token**
   - Token may be corrupted
   - Make sure you copied full token
   - Login again

4. **Token revoked (logged out)**
   - Token in blacklist
   - Must login again

#### Error: "403 Forbidden"

**Symptom:**
```json
{
  "code": 403,
  "message": "Access denied"
}
```

**Cause:** User lacks required role/permission

**Solution:**
1. Check endpoint requirements (ADMIN? USER?)
2. Check user roles: `GET /users/my-info`
3. Add role to user in database:
   ```sql
   INSERT INTO user_roles (user_id, role_name) VALUES ('user-id', 'ADMIN');
   ```

#### Error: "Invalid JWT Signature"

**Symptom:**
```
JWT signature verification failed
```

**Cause:** Services using different signing keys

**Solution:**
1. Check `jwt.signing-key` in all `application.yaml` files
2. Must be EXACTLY the same in:
   - api-gateway/src/main/resources/application.yaml
   - auth-service/src/main/resources/application.yaml
   - main-service/src/main/resources/application.yaml

#### Error: Database Connection Failed

**Symptom:**
```
org.postgresql.util.PSQLException: Connection refused
```

**Solutions:**

1. **PostgreSQL not running**
   ```bash
   # Windows
   services.msc  # Start PostgreSQL service
   
   # Linux
   sudo systemctl start postgresql
   
   # Mac
   brew services start postgresql
   ```

2. **Wrong credentials**
   - Check `application.yaml`:
     ```yaml
     spring:
       datasource:
         username: postgres
         password: 123456  # ← Correct?
     ```

3. **Database doesn't exist**
   ```sql
   CREATE DATABASE auth_service;
   CREATE DATABASE genzf;
   ```

#### Error: "Column does not exist"

**Symptom:**
```
ERROR: column "asset_home" does not exist
```

**Cause:** Database schema out of sync with code

**Solution:**
1. **Option 1 - Let Hibernate create it:**
   ```yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: update  # ← Make sure this is set
   ```
   Restart service.

2. **Option 2 - Manual SQL:**
   ```sql
   ALTER TABLE assets ADD COLUMN asset_home BOOLEAN DEFAULT false;
   ```

### 10.2 How to Read Logs

#### Log Levels

```
2024-01-14 10:30:45.123  INFO --- [main] AuthServiceApplication : Starting...
                                 ↑
                                 Level

INFO  - Normal operation
WARN  - Potential issue
ERROR - Something went wrong
DEBUG - Detailed info (only if enabled)
```

#### Important Log Patterns

**Service Started Successfully:**
```
Started AuthServiceApplication in 3.456 seconds
```

**Database Connection:**
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

**JWT Validation:**
```
DEBUG GatewayJwtAuthenticationFilter : JWT validation failed: Token expired
```

**SQL Queries (when show-sql: true):**
```
Hibernate: select user0_.id as id1_0_0_ from users user0_ where user0_.username=?
```

#### Finding Errors

Look for:
```
ERROR 12345 --- [...] : <error message>
        at com.qnit18.auth_service.service.UserService.createUser(UserService.java:45)
        at ...
```

This tells you:
- **ERROR**: Something went wrong
- **12345**: Process ID
- **Error message**: What went wrong
- **Stack trace**: Where it happened

### 10.3 Database Connection Issues

#### Check PostgreSQL is Running

```bash
# Windows
sc query postgresql

# Linux
systemctl status postgresql

# Mac
brew services list | grep postgresql
```

#### Test Connection

```bash
psql -U postgres -h localhost -p 5432
```

If this works, PostgreSQL is running.

#### Check Database Exists

```sql
\list  -- or \l

-- Should see:
-- auth_service
-- genzf
```

#### Reset Database (Last Resort)

```sql
-- WARNING: This deletes all data!

DROP DATABASE IF EXISTS auth_service;
DROP DATABASE IF EXISTS genzf;

CREATE DATABASE auth_service;
CREATE DATABASE genzf;
```

Then restart services to recreate tables.

### 10.4 JWT Token Issues

#### Decode JWT (Online)

Go to https://jwt.io/ and paste your token to see contents.

#### Check Token Expiry

```javascript
// In browser console
const token = "eyJhbGc...";
const payload = JSON.parse(atob(token.split('.')[1]));
console.log(new Date(payload.exp * 1000));  // Expiry time
console.log(payload);  // Full payload
```

#### Clear Token Blacklist

If you think token should be valid but getting 401:

```sql
-- Connect to auth_service database
\c auth_service

-- Check blacklist
SELECT * FROM invalided_token;

-- Clear blacklist (CAREFUL!)
DELETE FROM invalided_token WHERE expiry_time < NOW();
```

### 10.5 Port Already in Use

**Symptom:**
```
Port 8080 is already in use
```

**Solutions:**

1. **Stop the process:**
   ```bash
   # Windows
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F
   
   # Linux/Mac
   lsof -i :8080
   kill -9 <PID>
   ```

2. **Change port:**
   Edit `application.yaml`:
   ```yaml
   server:
     port: 8090  # Use different port
   ```

---

## 11. Best Practices

### 11.1 Code Organization

1. **Keep controllers thin**
   - Controllers should only handle HTTP
   - Move logic to services

2. **Use DTOs**
   - Don't expose entities directly
   - Use separate DTOs for requests and responses

3. **One class, one responsibility**
   - UserService handles users
   - AuthenticationService handles authentication
   - Don't mix concerns

### 11.2 Security Practices

1. **Never hardcode secrets**
   ```java
   // Bad
   String secret = "my-secret-key";
   
   // Good
   @Value("${jwt.signing-key}")
   String secret;
   ```

2. **Always hash passwords**
   - Never store plain text passwords
   - Use BCrypt (already configured)

3. **Validate all inputs**
   ```java
   @PostMapping
   public Response create(@RequestBody @Valid Request request) {
       // @Valid triggers validation
   }
   ```

4. **Use HTTPS in production**
   - HTTP is not secure
   - Use SSL/TLS certificates

### 11.3 Error Handling

1. **Use specific error codes**
   ```java
   public enum ErrorCode {
       USER_NOT_FOUND(2001, "User not found"),
       INVALID_PASSWORD(2002, "Invalid password"),
       // ...
   }
   ```

2. **Don't leak sensitive info**
   ```java
   // Bad
   throw new Exception("User john with password abc123 not found");
   
   // Good
   throw new AppException(ErrorCode.USER_NOT_FOUND);
   ```

3. **Log errors**
   ```java
   try {
       // risky operation
   } catch (Exception e) {
       log.error("Error processing user: {}", userId, e);
       throw new AppException(ErrorCode.PROCESSING_ERROR);
   }
   ```

### 11.4 Logging Practices

1. **Use appropriate log levels**
   ```java
   log.debug("User details: {}", user);    // Detailed debugging
   log.info("User created: {}", userId);   // Normal operations
   log.warn("User not found: {}", userId); // Potential issues
   log.error("Failed to create user", e);  // Errors
   ```

2. **Include context**
   ```java
   // Bad
   log.error("Error occurred");
   
   // Good
   log.error("Error creating user {} in portfolio {}", userId, portfolioId, exception);
   ```

3. **Don't log sensitive data**
   ```java
   // Bad
   log.info("User password: {}", password);
   
   // Good
   log.info("User authenticated: {}", username);
   ```

---

## 12. Next Steps

### 12.1 What to Learn Next

Now that you understand the basics, here's what to learn next:

#### Beginner Level (You are here!)
- ✅ Understand microservices architecture
- ✅ Know how services communicate
- ✅ Understand JWT authentication
- ✅ Can run and test the system

#### Intermediate Level
- Study Spring Boot in depth
- Learn Spring Data JPA queries
- Understand transaction management
- Learn about caching strategies
- Study API design best practices
- Learn database optimization

#### Advanced Level
- Implement service discovery (Eureka)
- Add circuit breakers (Resilience4j)
- Implement distributed tracing (Sleuth/Zipkin)
- Set up centralized logging (ELK stack)
- Learn Kubernetes for deployment
- Implement event-driven architecture

### 12.2 Recommended Resources

#### Spring Boot
- Official docs: https://spring.io/projects/spring-boot
- Spring Boot Tutorial: https://www.baeldung.com/spring-boot
- Spring in Action (book)

#### JWT
- JWT.io: https://jwt.io/introduction
- JWT Best Practices: https://tools.ietf.org/html/rfc8725

#### Microservices
- Microservices.io: https://microservices.io/
- Building Microservices (book by Sam Newman)

#### PostgreSQL
- Official docs: https://www.postgresql.org/docs/
- PostgreSQL Tutorial: https://www.postgresqltutorial.com/

### 12.3 Project-Specific Conventions

#### Naming Conventions

- **Classes**: PascalCase (`UserService`, `AuthenticationController`)
- **Methods**: camelCase (`createUser`, `getPortfolioById`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_LOGIN_ATTEMPTS`, `DEFAULT_PAGE_SIZE`)
- **Packages**: lowercase (`com.qnit18.auth_service.service`)

#### Commit Message Format

```
[TYPE] Brief description

Detailed explanation if needed

TYPE can be:
- feat: New feature
- fix: Bug fix
- docs: Documentation
- refactor: Code refactoring
- test: Adding tests
- chore: Maintenance tasks
```

Example:
```
feat: Add endpoint to get user portfolios

- Added GET /portfolios/user/{userId}
- Updated PortfolioService with new method
- Added unit tests
```

#### Code Review Checklist

Before submitting code:
- [ ] Code compiles without errors
- [ ] Tests pass
- [ ] No hardcoded values
- [ ] Proper error handling
- [ ] Logging added
- [ ] Documentation updated
- [ ] No sensitive data in logs
- [ ] Follows naming conventions

---

## 13. Glossary

**API (Application Programming Interface)**: A way for programs to communicate with each other

**Authentication**: Proving who you are (like showing your ID)

**Authorization**: Checking what you're allowed to do (like checking your ticket type)

**Bean**: An object managed by Spring framework

**CORS (Cross-Origin Resource Sharing)**: Allows web apps from different domains to access the API

**DTO (Data Transfer Object)**: Object used to transfer data between layers

**Entity**: A class that maps to a database table

**Endpoint**: A specific URL that handles requests (like `/users` or `/login`)

**JWT (JSON Web Token)**: A token format for authentication, contains user info

**JPA (Java Persistence API)**: Standard for working with databases in Java

**Microservice**: Small, independent service that does one thing well

**ORM (Object-Relational Mapping)**: Converts between database tables and Java objects

**Repository**: Class that handles database operations

**REST (Representational State Transfer)**: A style of building web APIs

**Service Layer**: Where business logic lives

**Spring Boot**: Framework for building Java applications quickly

**Spring Security**: Framework for handling security in Spring apps

**Token**: A piece of data that proves authentication (like a concert wristband)

---

## Congratulations! 🎉

You've completed the beginner's guide to GenZF! You now understand:

✅ What microservices are and why we use them
✅ How the 3 services work together
✅ How authentication works with JWT
✅ How to set up and run the system
✅ How to add new features
✅ How to troubleshoot common issues

**Remember:** It's normal to feel overwhelmed at first. Take your time, experiment, break things (in dev!), and learn from errors. Every expert was once a beginner!

**Questions?** Check the other documentation files:
- `ARCHITECTURE.md` - Detailed architecture
- `QUICK_START.md` - Quick commands
- `IMPLEMENTATION_SUMMARY.md` - Implementation details

**Happy coding!** 🚀

