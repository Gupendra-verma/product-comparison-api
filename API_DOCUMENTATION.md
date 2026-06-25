# Product Comparison API - Comprehensive Documentation

## Overview

The Product Comparison API is a comprehensive RESTful API that enables users to browse, compare, and review products. Built with Spring Boot 3.4.3 and secured with JWT authentication, this API provides a robust platform for product management and comparison.

**API Version:** 1.0.0  
**Base URL:** `http://localhost:8080`  
**Documentation Server:** `http://localhost:8080/swagger-ui.html`  
**OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
   - [Authentication Endpoints](#authentication-endpoints)
   - [Product Endpoints](#product-endpoints)
   - [Review Endpoints](#review-endpoints)
   - [Specification Endpoints](#specification-endpoints)
   - [Comparison Endpoints](#comparison-endpoints)
4. [Error Handling](#error-handling)
5. [Rate Limiting & Best Practices](#rate-limiting--best-practices)
6. [Response Formats](#response-formats)
7. [Examples](#examples)

---

## Getting Started

### Prerequisites

- Java 21 or higher
- Spring Boot 3.4.3
- PostgreSQL database
- Redis (for caching)

### Quick Start

1. **Start the Application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

2. **Access Swagger UI**
   Navigate to: `http://localhost:8080/swagger-ui.html`

3. **Get API Specification**
   ```
   http://localhost:8080/v3/api-docs
   ```

---

## Authentication

### JWT Token Flow

The API uses Bearer token authentication via JSON Web Tokens (JWT). All protected endpoints require a valid JWT token in the Authorization header.

### Obtaining a Token

#### 1. Register New User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "john_doe",
  "role": "USER"
}
```

#### 2. Login with Existing Account
```http
POST /auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securePassword123"
}
```

#### 3. Using the Token

Include the token in the `Authorization` header for all subsequent requests:

```http
GET /products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Role-Based Access Control

The API supports two roles:

- **USER**: Regular users who can view products, add reviews
- **ADMIN**: Administrators who can manage products and specifications

---

## API Endpoints

### Authentication Endpoints

#### Register New User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "string (3-50 chars)",
  "password": "string (6-100 chars)"
}
```

**Response:** `200 OK`
```json
{
  "token": "string",
  "tokenType": "Bearer",
  "username": "string",
  "role": "USER"
}
```

**Error Responses:**
- `400`: Invalid input or user already exists
- `500`: Internal server error

---

#### Register Admin User
```http
POST /auth/register/admin
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

**Response:** `200 OK` (same as user registration)

**Error Responses:**
- `400`: Invalid input
- `403`: Insufficient permissions
- `500`: Internal server error

---

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

**Response:** `200 OK` (same as registration)

**Error Responses:**
- `400`: Malformed request
- `401`: Invalid credentials
- `500`: Internal server error

---

### Product Endpoints

#### Get All Products (Paginated)
```http
GET /products?page=0&size=10&sortBy=id&sortDir=asc&minPrice=100&maxPrice=1000&brand=Apple&categoryName=Electronics
```

**Query Parameters:**
- `page` (optional, default: 0): Zero-based page number
- `size` (optional, default: 10): Records per page
- `sortBy` (optional, default: id): Field to sort by
- `sortDir` (optional, default: asc): Sort direction (asc/desc)
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter
- `brand` (optional): Brand filter
- `categoryName` (optional): Category name filter

**Response:** `200 OK`
```json
{
  "message": "Products retrieved successfully",
  "data": {
    "products": [
      {
        "id": 1,
        "name": "Product Name",
        "brand": "Brand Name",
        "price": 999.99,
        "category": "Electronics",
        "description": "Product description"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "last": false
  }
}
```

---

#### Search Products
```http
GET /products/search?keyword=iphone&page=0&size=10&sortBy=price&sortDir=asc
```

**Query Parameters:**
- `keyword` (required, min: 1 char): Search term
- `page` (optional, default: 0): Zero-based page number
- `size` (optional, default: 10): Records per page
- `sortBy` (optional, default: id): Field to sort by
- `sortDir` (optional, default: asc): Sort direction

**Response:** `200 OK` (same structure as Get All Products)

---

#### Get Product by ID
```http
GET /products/{productId}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "name": "iPhone 14 Pro",
  "brand": "Apple",
  "price": 999.99,
  "category": "Electronics",
  "description": "Latest flagship smartphone",
  "specifications": [
    {
      "id": 1,
      "name": "Display",
      "value": "6.1 inch OLED"
    }
  ],
  "reviews": [
    {
      "productId": 1,
      "userName": "john_doe",
      "comment": "Great phone!",
      "rating": 4.5
    }
  ]
}
```

**Error Responses:**
- `404`: Product not found
- `500`: Internal server error

---

#### Add Product (Admin Only)
```http
POST /products/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "New Product",
  "brand": "Brand Name",
  "price": 299.99,
  "category": "Electronics",
  "description": "Product description",
  "stock": 100
}
```

**Response:** `200 OK`

**Error Responses:**
- `400`: Invalid product data
- `403`: Admin access required
- `500`: Internal server error

---

#### Bulk Add Products (Admin Only)
```http
POST /products/bulkadd
Authorization: Bearer <token>
Content-Type: application/json

[
  {
    "name": "Product 1",
    "brand": "Brand",
    "price": 299.99,
    "category": "Electronics",
    "description": "Description 1",
    "stock": 50
  },
  {
    "name": "Product 2",
    "brand": "Brand",
    "price": 399.99,
    "category": "Electronics",
    "description": "Description 2",
    "stock": 75
  }
]
```

**Response:** `200 OK`

---

#### Delete Product (Soft Delete - Admin Only)
```http
DELETE /products/{productId}
Authorization: Bearer <token>
```

**Response:** `204 No Content`

---

#### Permanently Delete Product (Hard Delete - Admin Only)
```http
DELETE /products/{productId}/hard
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "message": "product with id 1 has been permanently deleted"
}
```

---

### Review Endpoints

#### Add Review (User Only)
```http
POST /products/{productId}/reviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "comment": "Great product!",
  "rating": 4.5
}
```

**Response:** `200 OK`

**Error Responses:**
- `400`: Invalid review data
- `401`: User must be logged in
- `404`: Product not found
- `500`: Internal server error

---

#### Get Product Reviews
```http
GET /products/{productId}/reviews
```

**Response:** `200 OK`
```json
[
  {
    "productId": 1,
    "userName": "john_doe",
    "comment": "Excellent quality",
    "rating": 5.0
  },
  {
    "productId": 1,
    "userName": "jane_doe",
    "comment": "Good value for money",
    "rating": 4.0
  }
]
```

---

#### Update Review (User Only)
```http
PUT /products/{productId}/reviews
Authorization: Bearer <token>
Content-Type: application/json

{
  "comment": "Updated comment",
  "rating": 4.8
}
```

**Response:** `200 OK`

---

#### Delete Review (User Only)
```http
DELETE /products/{productId}/reviews
Authorization: Bearer <token>
```

**Response:** `200 OK`

---

### Specification Endpoints

#### Add Product Specifications (Admin Only)
```http
POST /products/{productId}/specifications
Authorization: Bearer <token>
Content-Type: application/json

[
  {
    "name": "Display",
    "value": "6.1 inch OLED"
  },
  {
    "name": "Processor",
    "value": "Apple A16 Bionic"
  },
  {
    "name": "RAM",
    "value": "6 GB"
  }
]
```

**Response:** `200 OK`

---

#### Get Product Specifications
```http
GET /products/{productId}/specifications
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Display",
    "value": "6.1 inch OLED"
  },
  {
    "id": 2,
    "name": "Processor",
    "value": "Apple A16 Bionic"
  }
]
```

---

### Comparison Endpoints

#### Compare Products
```http
GET /compare?productIds=1,2,3
```

**Query Parameters:**
- `productIds` (required): List of 2-4 product IDs to compare

**Response:** `200 OK`
```json
{
  "products": [
    {
      "id": 1,
      "name": "iPhone 14 Pro",
      "brand": "Apple"
    },
    {
      "id": 2,
      "name": "Samsung Galaxy S23",
      "brand": "Samsung"
    },
    {
      "id": 3,
      "name": "Google Pixel 7",
      "brand": "Google"
    }
  ],
  "rows": [
    {
      "attribute": "Display",
      "values": ["6.1 inch OLED", "6.1 inch AMOLED", "6.3 inch OLED"]
    },
    {
      "attribute": "Processor",
      "values": ["Apple A16 Bionic", "Snapdragon 8 Gen 2", "Google Tensor"]
    },
    {
      "attribute": "RAM",
      "values": ["6 GB", "8 GB", "8 GB"]
    }
  ]
}
```

**Error Responses:**
- `400`: Invalid product IDs or wrong count (must be 2-4)
- `404`: One or more products not found
- `500`: Internal server error

---

## Error Handling

### Error Response Format

All error responses follow this format:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input parameters",
  "path": "/products"
}
```

### Common Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Missing or invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error |

### Error Messages

- **Validation Errors**: Include field name and constraint violation
- **Authentication Errors**: Provide reason for failure
- **Authorization Errors**: Specify required role or permission

---

## Rate Limiting & Best Practices

### Best Practices

1. **Token Management**
   - Store tokens securely in your application
   - Refresh tokens before expiration
   - Never expose tokens in logs or error messages

2. **Pagination**
   - Use reasonable page sizes (10-100 records)
   - Implement pagination in your UI for large datasets
   - Sort results consistently

3. **Search Optimization**
   - Use specific keywords for better results
   - Combine filters for more precise searches
   - Implement result caching when appropriate

4. **Error Handling**
   - Implement exponential backoff for retries
   - Log errors for debugging
   - Provide user-friendly error messages

5. **Performance**
   - Use compression for large responses
   - Implement client-side caching
   - Batch operations when possible

---

## Response Formats

### Success Response

```json
{
  "message": "Operation completed successfully",
  "data": {
    // Response data varies by endpoint
  }
}
```

### Paginated Response

```json
{
  "message": "Data retrieved successfully",
  "data": {
    "products": [...],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "last": false
  }
}
```

### List Response

```json
[
  {
    "id": 1,
    // Item data
  },
  {
    "id": 2,
    // Item data
  }
]
```

---

## Examples

### Example 1: Complete Product Search & Review Flow

```bash
# 1. Register user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'

# Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "tokenType": "Bearer",
#   "username": "john_doe",
#   "role": "USER"
# }

# 2. Search for products
curl -X GET "http://localhost:8080/products/search?keyword=iphone&size=5" \
  -H "Accept: application/json"

# 3. Get product details
curl -X GET http://localhost:8080/products/1 \
  -H "Accept: application/json"

# 4. Add a review
curl -X POST http://localhost:8080/products/1/reviews \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "comment": "Excellent phone!",
    "rating": 4.5
  }'

# 5. View all reviews
curl -X GET http://localhost:8080/products/1/reviews \
  -H "Accept: application/json"
```

### Example 2: Product Comparison

```bash
# Compare 3 products
curl -X GET "http://localhost:8080/compare?productIds=1,2,3" \
  -H "Accept: application/json"
```

### Example 3: Admin Product Management

```bash
# Add new product (Admin only)
curl -X POST http://localhost:8080/products/add \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Smartphone",
    "brand": "Samsung",
    "price": 899.99,
    "category": "Electronics",
    "description": "Latest flagship device",
    "stock": 100
  }'

# Add specifications
curl -X POST http://localhost:8080/products/1/specifications \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "name": "Display",
      "value": "6.1 inch Dynamic AMOLED 2X"
    },
    {
      "name": "Processor",
      "value": "Snapdragon 8 Gen 2"
    }
  ]'
```

---

## API Testing

### Using Swagger UI

1. Navigate to `http://localhost:8080/swagger-ui.html`
2. Click on "Authorize" button
3. Enter your JWT token (format: `Bearer <token>`)
4. Click "Authorize" to apply to all requests
5. Test endpoints directly from the UI

### Using cURL

```bash
# Basic request without authentication
curl -X GET http://localhost:8080/products

# Request with authentication
curl -X GET http://localhost:8080/products/1/reviews \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# POST request
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass"}'
```

### Using Postman

1. Import the API: `http://localhost:8080/v3/api-docs`
2. Create a new request collection
3. Set authorization type to "Bearer Token"
4. Add your JWT token
5. Test various endpoints

---

## Development & Support

### Documentation Resources

- **OpenAPI Specification**: `http://localhost:8080/v3/api-docs`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Configuration File**: `src/main/java/com/example/ProductComparison/Config/OpenApiConfig.java`

### Common Issues

1. **CORS Errors**: Ensure your frontend and backend are properly configured
2. **Token Expiration**: Implement token refresh logic
3. **Validation Errors**: Check field constraints and data types
4. **Authorization Errors**: Verify user role and permissions

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2024-01-15 | Initial API release with full Swagger documentation |

---

**Last Updated:** January 15, 2024  
**API Status:** ✅ Production Ready
