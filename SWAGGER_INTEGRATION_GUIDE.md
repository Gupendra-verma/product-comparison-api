# Swagger Integration Guide

## Overview

This guide explains how to integrate with the Product Comparison API using the Swagger documentation and provides code examples for different programming languages.

---

## Table of Contents

1. [Accessing Swagger Documentation](#accessing-swagger-documentation)
2. [Understanding Swagger UI](#understanding-swagger-ui)
3. [JavaScript/Fetch Integration](#javascriptfetch-integration)
4. [Python Integration](#python-integration)
5. [JavaScript/Axios Integration](#javascriptaxios-integration)
6. [cURL Examples](#curl-examples)
7. [Postman Integration](#postman-integration)
8. [Best Practices](#best-practices)

---

## Accessing Swagger Documentation

### URLs

- **Swagger UI**: `http://localhost:8091/swagger-ui.html`
- **OpenAPI Specification (JSON)**: `http://localhost:8091/v3/api-docs`
- **OpenAPI Specification (YAML)**: `http://localhost:8091/v3/api-docs.yaml`

### What Each Provides

| Resource | Purpose |
|----------|---------|
| Swagger UI | Interactive browser-based API testing |
| JSON Spec | Machine-readable API specification (for tools/SDKs) |
| YAML Spec | Human-readable API specification |

---

## Understanding Swagger UI

### Layout

```
┌─────────────────────────────────────────┐
│ Authorize Button (top-right)            │
├─────────────────────────────────────────┤
│ Tag Sections (e.g., Authentication)    │
│ ├─ Method + Path                        │
│ ├─ Summary                              │
│ ├─ Try it out button                    │
│ ├─ Parameters                           │
│ ├─ Request body schema                  │
│ └─ Responses with schemas               │
└─────────────────────────────────────────┘
```

### Key Features

1. **Authorize Button**: Add JWT token for protected endpoints
2. **Try it out**: Test endpoints directly from the browser
3. **Schema View**: See request/response formats
4. **Example Values**: Pre-filled example data
5. **Response Display**: See status, headers, and body

---

## JavaScript/Fetch Integration

### Basic Setup

```javascript
// Configuration
const API_BASE_URL = 'http://localhost:8091';
let authToken = null;

// Store token after login
function setAuthToken(token) {
  authToken = token;
  localStorage.setItem('authToken', token); // Persist token
}

// Retrieve token on page load
function loadStoredToken() {
  authToken = localStorage.getItem('authToken');
}

// Generic API request function
async function apiRequest(method, endpoint, data = null) {
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(authToken && { 'Authorization': `Bearer ${authToken}` })
    }
  };

  if (data) {
    options.body = JSON.stringify(data);
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    
    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(`${response.status}: ${errorData.message || 'API Error'}`);
    }

    return await response.json();
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}
```

### Authentication Example

```javascript
// Register
async function register(username, password) {
  const response = await apiRequest('POST', '/auth/register', {
    username,
    password
  });
  setAuthToken(response.token);
  return response;
}

// Login
async function login(username, password) {
  const response = await apiRequest('POST', '/auth/login', {
    username,
    password
  });
  setAuthToken(response.token);
  return response;
}

// Usage
register('john_doe', 'password123')
  .then(res => console.log('Registered:', res))
  .catch(err => console.error('Registration failed:', err));
```

### Product Operations Example

```javascript
// Get all products
async function getAllProducts(page = 0, size = 10) {
  return apiRequest(
    'GET',
    `/products?page=${page}&size=${size}&sortBy=id&sortDir=asc`
  );
}

// Search products
async function searchProducts(keyword, page = 0, size = 10) {
  return apiRequest(
    'GET',
    `/products/search?keyword=${keyword}&page=${page}&size=${size}`
  );
}

// Get product by ID
async function getProductById(productId) {
  return apiRequest('GET', `/products/${productId}`);
}

// Add review
async function addReview(productId, comment, rating) {
  return apiRequest('POST', `/products/${productId}/reviews`, {
    comment,
    rating
  });
}

// Get reviews
async function getReviews(productId) {
  return apiRequest('GET', `/products/${productId}/reviews`);
}

// Compare products
async function compareProducts(productIds) {
  const ids = productIds.join(',');
  return apiRequest('GET', `/compare?productIds=${ids}`);
}

// Usage Examples
(async () => {
  try {
    loadStoredToken();

    // Get products
    const products = await getAllProducts(0, 5);
    console.log('Products:', products);

    // Search
    const searchResults = await searchProducts('iphone');
    console.log('Search results:', searchResults);

    // Get product details
    const product = await getProductById(1);
    console.log('Product:', product);

    // Add review (requires auth)
    const review = await addReview(1, 'Great product!', 4.5);
    console.log('Review added:', review);

    // Compare products
    const comparison = await compareProducts([1, 2, 3]);
    console.log('Comparison:', comparison);

  } catch (error) {
    console.error('Error:', error);
  }
})();
```

---

## Python Integration

### Using requests Library

```python
import requests
import json
from typing import Optional, Dict, Any

class ProductComparisonAPI:
    def __init__(self, base_url: str = 'http://localhost:8091'):
        self.base_url = base_url
        self.auth_token = None
        self.headers = {'Content-Type': 'application/json'}

    def set_auth_token(self, token: str):
        """Set authentication token"""
        self.auth_token = token
        self.headers['Authorization'] = f'Bearer {token}'

    def _request(self, method: str, endpoint: str, 
                data: Optional[Dict] = None) -> Dict[str, Any]:
        """Make API request"""
        url = f'{self.base_url}{endpoint}'
        
        try:
            if method == 'GET':
                response = requests.get(url, headers=self.headers)
            elif method == 'POST':
                response = requests.post(url, headers=self.headers, json=data)
            elif method == 'PUT':
                response = requests.put(url, headers=self.headers, json=data)
            elif method == 'DELETE':
                response = requests.delete(url, headers=self.headers)
            else:
                raise ValueError(f'Unsupported method: {method}')

            response.raise_for_status()
            return response.json()
        
        except requests.exceptions.RequestException as e:
            print(f'API Error: {e}')
            raise

    # Authentication endpoints
    def register(self, username: str, password: str) -> Dict:
        """Register new user"""
        response = self._request('POST', '/auth/register', {
            'username': username,
            'password': password
        })
        self.set_auth_token(response['token'])
        return response

    def login(self, username: str, password: str) -> Dict:
        """Login user"""
        response = self._request('POST', '/auth/login', {
            'username': username,
            'password': password
        })
        self.set_auth_token(response['token'])
        return response

    # Product endpoints
    def get_all_products(self, page: int = 0, size: int = 10) -> Dict:
        """Get all products with pagination"""
        return self._request(
            'GET',
            f'/products?page={page}&size={size}&sortBy=id&sortDir=asc'
        )

    def search_products(self, keyword: str, page: int = 0, 
                       size: int = 10) -> Dict:
        """Search products by keyword"""
        return self._request(
            'GET',
            f'/products/search?keyword={keyword}&page={page}&size={size}'
        )

    def get_product(self, product_id: int) -> Dict:
        """Get product by ID"""
        return self._request('GET', f'/products/{product_id}')

    # Review endpoints
    def add_review(self, product_id: int, comment: str, 
                   rating: float) -> Dict:
        """Add review to product"""
        return self._request('POST', f'/products/{product_id}/reviews', {
            'comment': comment,
            'rating': rating
        })

    def get_reviews(self, product_id: int) -> list:
        """Get all reviews for a product"""
        return self._request('GET', f'/products/{product_id}/reviews')

    # Comparison endpoints
    def compare_products(self, product_ids: list) -> Dict:
        """Compare multiple products"""
        ids = ','.join(map(str, product_ids))
        return self._request('GET', f'/compare?productIds={ids}')


# Usage example
if __name__ == '__main__':
    api = ProductComparisonAPI()

    try:
        # Register
        reg_response = api.register('john_doe', 'password123')
        print(f'Registered: {reg_response["username"]}')

        # Get products
        products = api.get_all_products(page=0, size=5)
        print(f'Found {products["data"]["totalElements"]} products')

        # Search
        search = api.search_products('iphone')
        print(f'Search results: {len(search["data"]["products"])} items')

        # Get product details
        product = api.get_product(1)
        print(f'Product: {product["name"]} by {product["brand"]}')

        # Add review
        review = api.add_review(1, 'Excellent quality!', 4.5)
        print('Review added')

        # Compare products
        comparison = api.compare_products([1, 2, 3])
        print(f'Comparing {len(comparison["products"])} products')

    except Exception as e:
        print(f'Error: {e}')
```

---

## JavaScript/Axios Integration

### Setup with Axios

```javascript
import axios from 'axios';

class ProductComparisonAPI {
  constructor(baseURL = 'http://localhost:8091') {
    this.api = axios.create({
      baseURL,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // Add response interceptor for error handling
    this.api.interceptors.response.use(
      response => response.data,
      error => {
        console.error('API Error:', error.response?.data || error.message);
        throw error;
      }
    );
  }

  setAuthToken(token) {
    this.api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  // Authentication
  register(username, password) {
    return this.api.post('/auth/register', { username, password });
  }

  login(username, password) {
    return this.api.post('/auth/login', { username, password });
  }

  // Products
  getAllProducts(page = 0, size = 10) {
    return this.api.get('/products', {
      params: { page, size, sortBy: 'id', sortDir: 'asc' }
    });
  }

  searchProducts(keyword, page = 0, size = 10) {
    return this.api.get('/products/search', {
      params: { keyword, page, size }
    });
  }

  getProduct(productId) {
    return this.api.get(`/products/${productId}`);
  }

  // Reviews
  addReview(productId, comment, rating) {
    return this.api.post(`/products/${productId}/reviews`, {
      comment,
      rating
    });
  }

  getReviews(productId) {
    return this.api.get(`/products/${productId}/reviews`);
  }

  // Comparison
  compareProducts(productIds) {
    return this.api.get('/compare', {
      params: { productIds: productIds.join(',') }
    });
  }
}

// Usage
const api = new ProductComparisonAPI();

(async () => {
  try {
    const login = await api.login('john_doe', 'password123');
    api.setAuthToken(login.token);

    const products = await api.getAllProducts();
    console.log('Products:', products);

    const reviews = await api.getReviews(1);
    console.log('Reviews:', reviews);

    const comparison = await api.compareProducts([1, 2, 3]);
    console.log('Comparison:', comparison);
  } catch (error) {
    console.error('Error:', error);
  }
})();
```

---

## cURL Examples

### Register and Login

```bash
# Register
curl -X POST http://localhost:8091/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }' \
  | jq '.token' -r > token.txt

# Save token to variable
TOKEN=$(cat token.txt)

# Subsequent requests with token
curl -X GET http://localhost:8091/products \
  -H "Authorization: Bearer $TOKEN"
```

### Common Operations

```bash
# Get all products
curl -X GET "http://localhost:8091/products?page=0&size=10" \
  -H "Accept: application/json"

# Search products
curl -X GET "http://localhost:8091/products/search?keyword=iphone" \
  -H "Accept: application/json"

# Get product details
curl -X GET http://localhost:8091/products/1 \
  -H "Accept: application/json"

# Add review (requires token)
curl -X POST http://localhost:8091/products/1/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "comment": "Great product!",
    "rating": 4.5
  }'

# Compare products
curl -X GET "http://localhost:8091/compare?productIds=1,2,3" \
  -H "Accept: application/json"
```

---

## Postman Integration

### Import OpenAPI Spec

1. Open Postman
2. Click **Import**
3. Select **Link**
4. Enter: `http://localhost:8091/v3/api-docs`
5. Click **Continue**
6. Click **Import**

### Setup Authorization

1. In Postman, go to **Collections**
2. Right-click your collection → **Edit**
3. Go to **Authorization** tab
4. Select **Bearer Token**
5. Add your JWT token
6. Click **Update**

### Test Endpoints

1. Select any endpoint from the imported collection
2. Update parameters if needed
3. Click **Send**
4. View response

---

## Best Practices

### 1. Error Handling

```javascript
async function safeApiCall(apiFunction) {
  try {
    return await apiFunction();
  } catch (error) {
    if (error.response?.status === 401) {
      // Token expired - redirect to login
      redirectToLogin();
    } else if (error.response?.status === 403) {
      // Insufficient permissions
      showError('You do not have permission for this action');
    } else if (error.response?.status === 404) {
      // Resource not found
      showError('Resource not found');
    } else {
      // Generic error
      showError('An error occurred. Please try again.');
    }
  }
}
```

### 2. Token Management

```javascript
// Refresh token periodically
setInterval(async () => {
  if (isNearExpiration(authToken)) {
    const newToken = await refreshToken();
    setAuthToken(newToken);
  }
}, 60000); // Check every minute
```

### 3. Request Validation

```javascript
function validateProductIds(ids) {
  if (!Array.isArray(ids) || ids.length < 2 || ids.length > 4) {
    throw new Error('Must provide 2-4 product IDs');
  }
  return ids;
}

// Usage
try {
  const comparison = await compareProducts(
    validateProductIds([1, 2, 3])
  );
} catch (error) {
  console.error('Validation error:', error);
}
```

### 4. Pagination Helper

```javascript
async function* paginateProducts(pageSize = 10) {
  let page = 0;
  let hasMore = true;

  while (hasMore) {
    const response = await getAllProducts(page, pageSize);
    yield response.data.products;

    hasMore = !response.data.last;
    page++;
  }
}

// Usage
(async () => {
  for await (const products of paginateProducts()) {
    console.log('Processing batch:', products);
  }
})();
```

---

## Summary

| Language | Best Library | Complexity |
|----------|--------------|-----------|
| JavaScript | fetch API | Easy |
| JavaScript | Axios | Medium |
| Python | requests | Easy |
| cURL | Built-in | Medium |
| Postman | GUI | Easy |

---

**For more information, see:**
- `API_DOCUMENTATION.md` - Full API reference
- `SWAGGER_QUICK_START.md` - Quick start guide
- Swagger UI: `http://localhost:8091/swagger-ui.html`
