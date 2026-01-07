# Swagger API Documentation & Example Data Guide

## Swagger UI Access

After starting the main-service application, you can access the Swagger UI at:

- **Swagger UI**: `http://localhost:8181/genzf/swagger-ui.html`
- **API Docs (JSON)**: `http://localhost:8181/genzf/api-docs`

## Example Data

The application automatically initializes example data on first startup. The following data is created:

### Users (3)
- `john_doe`
- `jane_smith`
- `bob_wilson`

### Assets (5)
1. **XAU/USD** (Gold) - Commodity
2. **BTC/USD** (Bitcoin) - Crypto
3. **EUR/USD** (Euro/US Dollar) - Forex
4. **XAG/USD** (Silver) - Commodity
5. **ETH/USD** (Ethereum) - Crypto

### Portfolios (3)
- One portfolio for each user with various asset holdings

### Asset Holdings (7)
- **john_doe**: Gold (10.5 oz), Bitcoin (0.5 BTC)
- **jane_smith**: Ethereum (5.0 ETH), Silver (100.0 oz)
- **bob_wilson**: Gold (25.0 oz), EUR/USD (50,000), Bitcoin (1.0 BTC - deleted)

### Budget Rules (3)
- Each user has a budget rule with Needs/Wants/Savings allocations
- **john_doe**: $5,000/month (50/30/20)
- **jane_smith**: $7,500/month (40/30/30)
- **bob_wilson**: $10,000/month (50/25/25)

### Chart Data
- Gold: 1D and 1W timeframes with sample series items
- Bitcoin: 1D and 1M timeframes with sample series items

## Testing with Swagger UI

1. Start the application
2. Navigate to `http://localhost:8181/genzf/swagger-ui.html`
3. Explore the API endpoints organized by tags:
   - **Asset Management**: CRUD operations for trading assets
   - **User Management**: User account operations
   - **Portfolio Management**: Portfolio and net worth calculations
   - **Budget Rule Management**: Budget allocation management
   - **Asset Holdings Management**: User asset holdings in portfolios
   - **Chart Data**: Historical price data for charts
   - **Series Items**: Price/timestamp data points

4. Use the "Try it out" button on any endpoint to test
5. Example requests are pre-filled with example data

## Important Notes

- Example data is only created if the database is empty (no existing users)
- To reset example data, clear the database and restart the application
- All endpoints return responses wrapped in `ApiBaseResponse` format
- UUIDs are auto-generated - use the GET endpoints to find IDs for testing

## Example API Calls

### Get All Assets
```
GET /genzf/assets
```

### Get User Portfolio
```
GET /genzf/portfolios/user/{userId}
```

### Get Budget Rule by User
```
GET /genzf/budget-rules/user/{userId}
```

### Get Asset Holdings
```
GET /genzf/asset-users/portfolio/{portfolioId}
```

