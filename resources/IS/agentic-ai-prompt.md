# Agent Instructions: Create Account-Profile API

## Objective

Create an OpenAPI 3.0.1 compliant "Account-Profile" API specification for customer account management.

The API should provide two core capabilities:
- **Search**: Enable searching for customer data by name pattern
- **Retrieve**: Provide a comprehensive overview of all customer attributes for a given customer ID, including:
  - Customer name
  - Homepage URL
  - Customer type
  - Bank details (bank name and IBAN)

## Requirements

### 1. API Operations

#### Operation 1: Search Customers by Name Pattern
- **Path**: `/customers/search`
- **Method**: GET
- **Query Parameter**: `namePattern` (string, required)
- **Response**: Array of customer summaries

#### Operation 2: Get Customer Master Data
- **Path**: `/customers/{customerId}`
- **Method**: GET
- **Path Parameter**: `customerId` (string, required)
- **Response**: Customer profile with name, homepage, type, and bank details (bank name, IBAN)

### 2. OAuth Security
- Type: `oauth2`
- Flow: `clientCredentials`
- Token URL: `https://auth.example.com/oauth/token`

#### Scopes
- `customer:read` - Read customer information
- `customer:search` - Search customers

#### Scope Assignment
- Search operation: `customer:search`
- Get customer details: `customer:read`

### 3. Sample Data

#### Search Response
```json
[{"customerId": "CUST-001", "name": "Acme Corporation", "type": "string"}]
```

#### Customer Details
```json
{
  "customerId": "CUST-001",
  "name": "Acme Corporation",
  "homepage": "https://www.acme-corp.example.com",
  "type": "string",
  "bankDetails": {"bankName": "Global Bank AG", "iban": "DE89370400440532013000"}
}
```

### 4. Additional Requirements
- OpenAPI version: `3.0.1`
- Global tag: `mcp.object.name:Customer`
- HTTP status codes: 200, 400, 401, 403, 404, 500
- Content type: `application/json`
- API metadata: Title "Account-Profile API", Version "1.0.0"
- Define reusable schemas in `components/schemas`