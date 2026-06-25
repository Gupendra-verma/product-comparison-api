# Product Comparison API - Quick Start Guide

## 🚀 Quick Access

### Swagger UI (Recommended for Testing)
```
http://localhost:8091/swagger-ui.html
```

### OpenAPI JSON Specification
```
http://localhost:8091/v3/api-docs
```

### Health Check
```
http://localhost:8091/products/check
```

---

## 📋 First Steps

### 1. Run the Application
```bash
cd d:\Spring Project\ProductComparison
mvn clean install
mvn spring-boot:run
```

The server starts on: **http://localhost:8091**

### 2. Open Swagger UI
Navigate to: `http://localhost:8091/swagger-ui.html`

You'll see all available endpoints organized by tags:
- 🔐 **Authentication** - Register, Login
- 📦 **Products** - Browse, Search, Manage Products
- ⭐ **Reviews** - View, Add, Update Reviews
- 📊 **Specifications** - Product Technical Details
- 🔄 **Product Comparison** - Compare 2-4 Products

---

## 🔐 Authentication Flow

### Step 1: Register a New Account

**In Swagger UI:**
1. Click on the **"Authentication"** section
2. Expand **"POST /auth/register"**
3. Click **"Try it out"**
4. Enter your credentials:
   ```json
   {
     "username": "myusername",
     "password": "mySecurePassword123"
   }
   ```
5. Click **"Execute"**
6. Copy the `token` from the response

### Step 2: Authorize Subsequent Requests

1. Click the **"Authorize"** button (top right)
2. Paste your token in the format: `Bearer <your-token-here>`
3. Click **"Authorize"** then **"Close"**
4. Now all requests will include your authorization

### Step 3: Explore Protected Endpoints

All endpoints requiring authentication will now work!

---

## 📦 Common API Workflows

### Workflow 1: Browse and Review Products

```
1. GET /products
   └─ View all products with pagination

2. GET /products/search?keyword=iphone
   └─ Search for specific products

3. GET /products/{productId}
   └─ View detailed product information

4. GET /products/{productId}/reviews
   └─ See customer reviews

5. POST /products/{productId}/reviews
   └─ Add your own review
```

### Workflow 2: Compare Products

```
1. GET /compare?productIds=1,2,3
   └─ Compare 2-4 products side-by-side
   └─ View specifications in tabular format
```

### Workflow 3: Admin - Add Products

**Admin Registration (requires key):**
```
POST /auth/register/admin
with admin key: admin-key-0910
```

**Then Manage Products:**
```
1. POST /products/add
   └─ Add a single product

2. POST /products/bulkadd
   └─ Add multiple products at once

3. POST /products/{productId}/specifications
   └─ Add product specifications

4. DELETE /products/{productId}
   └─ Soft delete (archive) a product

5. DELETE /products/{productId}/hard
   └─ Permanently delete a product
```

---

## 🧪 Testing in Swagger UI

### To Test an Endpoint:

1. **Expand** the endpoint you want to test
2. Click **"Try it out"**
3. **Fill in** required parameters:
   - Path parameters (in URL)
   - Query parameters (in URL)
   - Request body (JSON)
4. Click **"Execute"**
5. View the **Response** (status code, headers, body)

### Example: Get All Products

1. Expand: `GET /products`
2. Click "Try it out"
3. Set parameters:
   - `page`: 0
   - `size`: 10
   - `sortBy`: price
   - `sortDir`: asc
4. Click "Execute"
5. See paginated results!

---

## 📝 Request/Response Examples

### Example 1: Login and Get Token

**Request:**
```bash
curl -X POST http://localhost:8091/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
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

### Example 2: Search Products

**Request:**
```bash
curl -X GET "http://localhost:8091/products/search?keyword=phone&page=0&size=5" \
  -H "Accept: application/json"
```

**Response:**
```json
{
  "message": "Products retrieved successfully",
  "data": {
    "products": [
      {
        "id": 1,
        "name": "iPhone 14 Pro",
        "brand": "Apple",
        "price": 999.99,
        "category": "Electronics"
      }
    ],
    "page": 0,
    "size": 5,
    "totalElements": 15,
    "totalPages": 3,
    "last": false
  }
}
```

### Example 3: Add a Review

**Request:**
```bash
curl -X POST http://localhost:8091/products/1/reviews \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "comment": "Excellent product quality!",
    "rating": 4.5
  }'
```

**Response:**
```json
{
  "message": "Review added successfully"
}
```

---

## ❌ Common Error Codes

| Code | Meaning | Solution |
|------|---------|----------|
| 200/201 | ✅ Success | Request completed successfully |
| 400 | ❌ Bad Request | Check your input parameters |
| 401 | 🔒 Unauthorized | Add valid JWT token to Authorization header |
| 403 | 🚫 Forbidden | Your role doesn't have permission |
| 404 | ❓ Not Found | Resource doesn't exist |
| 500 | ⚠️ Server Error | Check server logs |

---

## 💡 Pro Tips

### 1. Use Pagination
Always use pagination for large datasets:
```
?page=0&size=20
```

### 2. Filter Results
Combine multiple filters for precise results:
```
?minPrice=100&maxPrice=500&brand=Apple&categoryName=Electronics
```

### 3. Sort Results
Use sortBy and sortDir parameters:
```
?sortBy=price&sortDir=asc
```

### 4. Save Your Token
Store your token for reuse in multiple requests.

### 5. Use Swagger UI for Documentation
The Swagger UI shows:
- Parameter requirements
- Response schemas
- Example values
- Error possibilities

### 6. Check Response Schema
Click the response schema to see field descriptions and types.

---

## 🔧 Troubleshooting

### Issue: "Connection refused"
- **Solution:** Ensure the server is running on port 8091
- Check: `mvn spring-boot:run`

### Issue: "Invalid token" error
- **Solution:** Generate a new token by registering or logging in
- Paste token in Authorize dialog with "Bearer " prefix

### Issue: "403 Forbidden"
- **Solution:** You need admin role for that endpoint
- Register as admin using: `POST /auth/register/admin`
- Use the admin registration key from application.properties

### Issue: "404 Product not found"
- **Solution:** Product doesn't exist
- First, add products using the admin endpoints
- Or use products that already exist in the database

---

## 📚 API Documentation

For comprehensive API documentation, see:
- `API_DOCUMENTATION.md` - Complete API reference
- Swagger UI Schema - Auto-generated from code annotations

---

## 🎯 Key Endpoints Reference

| Method | Endpoint | Auth Required | Purpose |
|--------|----------|-----------------|---------|
| POST | `/auth/register` | No | Create user account |
| POST | `/auth/login` | No | Get JWT token |
| GET | `/products` | No | List all products |
| GET | `/products/search` | No | Search products |
| GET | `/products/{id}` | No | Get product details |
| POST | `/products/add` | ✅ Admin | Add new product |
| GET | `/products/{id}/reviews` | No | View reviews |
| POST | `/products/{id}/reviews` | ✅ User | Add review |
| GET | `/compare` | No | Compare products |
| POST | `/products/{id}/specifications` | ✅ Admin | Add specs |

---

## 📞 Support

- Check `API_DOCUMENTATION.md` for detailed endpoint documentation
- Review error messages in Swagger UI responses
- Check application logs for server-side errors
- Verify all required fields are provided in requests

---

**Last Updated:** January 15, 2024  
**Version:** 1.0.0
