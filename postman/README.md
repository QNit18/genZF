# GenZF API Collection

This Bruno collection contains all API endpoints for the GenZF microservices architecture.

## Structure

The collection is organized by service and resource type for easy navigation and maintenance:

```
postman/
├── collection.bru              # Root collection metadata
├── bruno.json                  # Bruno configuration
├── README.md                   # This file
├── environments/
│   └── genF.bru               # Environment variables (development)
├── auth-service/              # Authentication & Authorization Service
│   ├── folder.bru
│   ├── authentication/        # Login, logout, token operations
│   ├── users/                 # User management endpoints
│   ├── roles/                 # Role management endpoints
│   └── permissions/           # Permission management endpoints
├── main-service/              # Main Business Service
│   ├── folder.bru
│   ├── assets/                # Trading assets (Gold, Bitcoin, Forex, etc.)
│   └── portfolios/            # User portfolio management
└── health/                    # Health check endpoints
    ├── folder.bru
    ├── 01-health-auth-service.bru
    └── 02-health-main-service.bru
```

## Services Overview

### Auth Service (Port 8080)
Handles authentication, authorization, and user management:
- **Authentication**: Login, logout, token introspection, token refresh
- **Users**: CRUD operations for user accounts
- **Roles**: Role management with permissions
- **Permissions**: Permission management

### Main Service (Port 8081)
Core business logic for the GenZF platform:
- **Assets**: Trading assets management (Gold, Bitcoin, Forex, etc.)
- **Portfolios**: User investment portfolio management

### API Gateway (Port 8888)
Routes requests to appropriate microservices:
- `/auth-service/**` → Auth Service
- `/main-service/**` → Main Service

## Environment Variables

The `genF.bru` environment file contains the following variables:

| Variable | Description | Example Value |
|----------|-------------|---------------|
| `auth-service` | Direct auth service URL | `http://localhost:8080` |
| `main-service` | Direct main service URL | `http://localhost:8081/main-service` |
| `api-gateway-AuthSERVICE` | Auth service via gateway | `http://localhost:8888/auth-service` |
| `api-gateway-MainSERVICE` | Main service via gateway | `http://localhost:8888/main-service` |
| `token` | JWT token (set after login) | _(empty, set after login)_ |
| `sampleUserId` | Sample user ID for testing | _(empty, update with actual ID)_ |
| `sampleAssetId` | Sample asset ID for testing | _(empty, update with actual ID)_ |
| `samplePortfolioId` | Sample portfolio ID for testing | _(empty, update with actual ID)_ |
| `sampleRoleName` | Sample role name | `USER` |
| `samplePermissionName` | Sample permission name | `CREATE_DATA` |

## Getting Started

### 1. Start the Services

Ensure all services are running:
```bash
# Auth Service
cd server/auth-service
mvn spring-boot:run

# Main Service
cd server/main-service
mvn spring-boot:run

# API Gateway
cd server/api-gateway
mvn spring-boot:run
```

### 2. Verify Services are Running

Use the health check endpoints:
- Auth Service: `GET http://localhost:8080/actuator/health`
- Main Service: `GET http://localhost:8081/main-service/actuator/health`

### 3. Login to Get Token

Run `auth-service/authentication/01-login.bru` with credentials:
```json
{
  "username": "admin",
  "password": "admin"
}
```

Copy the returned token and update the `token` variable in `environments/genF.bru`.

### 4. Test Authenticated Endpoints

Now you can test all endpoints that require authentication. The token will be automatically included via the `{{token}}` variable.

## Naming Conventions

### File Names
- **Kebab-case**: All files use kebab-case (e.g., `get-user-by-id.bru`)
- **Numbered prefixes**: Files are prefixed with numbers for logical ordering (e.g., `01-`, `02-`)
- **Descriptive names**: Names indicate HTTP method + resource (e.g., `create-user`, `update-asset`)

### Request Names
- Follow pattern: `[Action] [Resource] [(Role)]`
- Examples: `Get All Users`, `Create Asset (ADMIN)`, `Delete Portfolio`

## Authentication

### Public Endpoints (No Auth Required)
- All authentication endpoints (login, introspect, refresh, logout)
- User registration (`POST /users`)
- Health checks
- Public asset browsing

### Authenticated Endpoints (Bearer Token Required)
- User management (get, update, delete)
- Role and permission management
- Portfolio operations
- Asset management (create, update, delete - ADMIN only)

### Setting Up Authentication

Most folders inherit authentication from their parent. The `token` variable is used:

```bru
auth {
  mode: bearer
  token: {{token}}
}
```

## Admin Endpoints

Certain endpoints require ADMIN role:
- Create/Update/Delete Assets
- Create/Delete Roles
- Create/Delete Permissions
- Delete Users

These are marked with `(ADMIN)` in their names.

## Query Parameters

Some endpoints support query parameters for filtering, pagination, and sorting:

### Get All Assets
- `q`: Search query (optional)
- `category`: Filter by category (FOREX, CRYPTO, COMMODITIES, STOCKS)
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `sort`: Sort field and direction (default: `changePercentage,desc`)

Example: `/main-service/assets?q=gold&category=COMMODITIES&page=0&size=10&sort=currentPrice,asc`

## Tips for Development

1. **Use the API Gateway**: Prefer `api-gateway-*` variables for testing as they route through the gateway (production-like)
2. **Update Sample IDs**: After creating resources, update the sample ID variables for easier testing
3. **Check Health First**: Always verify services are running before testing endpoints
4. **Order Matters**: Follow the numbered sequence in each folder for a logical testing flow
5. **Token Expiration**: Tokens expire after 1 hour (3600 seconds). Refresh or re-login if needed

## Troubleshooting

### 401 Unauthorized
- Token expired → Re-login or use refresh token endpoint
- Token not set → Ensure `{{token}}` variable is populated
- Insufficient permissions → Use ADMIN account for admin endpoints

### 404 Not Found
- Check service is running
- Verify URL includes correct context path (`/main-service` for main service)
- Ensure API Gateway is routing correctly

### Connection Refused
- Service not running → Start the service
- Wrong port → Check `application.yaml` for correct ports

## Collection Maintenance

When adding new endpoints:
1. Place in appropriate service/resource folder
2. Use numbered prefix for ordering
3. Follow naming conventions
4. Use environment variables for URLs
5. Add appropriate authentication mode
6. Include example request body with comments
7. Update this README if adding new resources

## API Documentation

For detailed API documentation with schemas and response examples:
- Main Service Swagger UI: `http://localhost:8081/main-service/swagger-ui.html`
- Main Service OpenAPI Docs: `http://localhost:8081/main-service/api-docs`

## Architecture

For detailed architecture information, see:
- `server/ARCHITECTURE.md` - System architecture overview
- `server/IMPLEMENTATION_SUMMARY.md` - Implementation details
- `server/QUICK_START.md` - Quick start guide

