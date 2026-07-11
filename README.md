[README.md](https://github.com/user-attachments/files/29925158/README.md)
# 🔍 Product Comparison API

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-green?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue?style=flat-square&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-Cache-red?style=flat-square&logo=redis)
![JWT](https://img.shields.io/badge/Auth-JWT-yellow?style=flat-square)
![Swagger](https://img.shields.io/badge/Docs-Swagger_UI-brightgreen?style=flat-square&logo=swagger)

A production-style RESTful backend that enables users to browse, search, compare, and review products. Built with Spring Boot 3.4.3, secured with JWT authentication and role-based access control, and optimised with Redis caching for high-frequency endpoints.

---

## ✨ Features

- **JWT Authentication** — stateless auth with Bearer tokens and role-based access (USER / ADMIN)
- **Product Management** — full CRUD with soft delete and hard delete (Admin only)
- **Bulk Operations** — bulk product insert endpoint for batch data management
- **Product Comparison** — compare 2–4 products side-by-side with spec-level attribute mapping
- **Product Specifications** — add and retrieve structured spec attributes per product
- **Reviews System** — users can add, update, and delete reviews with rating support
- **Paginated & Filtered Listings** — page, size, sortBy, sortDir, minPrice, maxPrice, brand, category filters
- **Keyword Search** — paginated full-text product search
- **Redis Caching** — caches high-frequency product listing and lookup responses
- **Global Exception Handling** — structured error responses with timestamp, status, and message
- **Swagger / OpenAPI 3.0** — interactive API docs at `/swagger-ui.html`
- **Input Validation** — `@Valid` annotations with field-level constraint messages

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          CLIENT                                  │
│                  (Postman / Frontend App)                        │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP Request
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING SECURITY LAYER                         │
│         JWT Filter → Token Validation → RBAC (USER/ADMIN)       │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CONTROLLER LAYER                             │
│   AuthController │ ProductController │ ComparisonController     │
│   ReviewController │ SpecificationController                    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                               │
│   Business logic, caching decisions, entity-to-DTO mapping     │
│                                                                  │
│         ┌──────────────────────────────────┐                    │
│         │         REDIS CACHE              │                    │
│         │   Product listings & lookups     │                    │
│         │   Cache-aside pattern with TTL   │                    │
│         └──────────────────────────────────┘                    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                              │
│            Spring Data JPA / Hibernate ORM                      │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                       PostgreSQL                                 │
│   users │ products │ specifications │ reviews                   │
└─────────────────────────────────────────────────────────────────┘

Cross-cutting:
  ┌─────────────────────────────────────────┐
  │  @ControllerAdvice — Global Exception   │
  │  Handler with structured error DTOs     │
  └─────────────────────────────────────────┘
  ┌─────────────────────────────────────────┐
  │  Swagger / OpenAPI 3.0                  │
  │  http://localhost:8080/swagger-ui.html  │
  └─────────────────────────────────────────┘
```

---

## 🗄️ Database Schema

```
┌──────────────────────────┐
│          users           │
├──────────────────────────┤
│ 🔑 id          BIGINT PK │
│    username    VARCHAR   │ ◄── unique
│    password    VARCHAR   │
│    role        ENUM      │ USER | ADMIN
└────────────┬─────────────┘
             │ 1
             │
             │ N
┌────────────▼─────────────┐         ┌──────────────────────────┐
│         reviews          │         │         products          │
├──────────────────────────┤         ├──────────────────────────┤
│ 🔑 id          BIGINT PK │         │ 🔑 id          BIGINT PK │
│ 🔗 product_id  FK        │◄───────►│    name        VARCHAR   │
│ 🔗 user_id     FK        │  N   1  │    brand       VARCHAR   │
│    comment     TEXT      │         │    price       DECIMAL   │
│    rating      DOUBLE    │         │    category    VARCHAR   │
└──────────────────────────┘         │    description TEXT      │
                                     │    stock       INTEGER   │
                                     │    deleted     BOOLEAN   │ soft delete
                                     └────────────┬─────────────┘
                                                  │ 1
                                                  │
                                                  │ N
                                     ┌────────────▼─────────────┐
                                     │      specifications       │
                                     ├──────────────────────────┤
                                     │ 🔑 id          BIGINT PK │
                                     │ 🔗 product_id  FK        │
                                     │    name        VARCHAR   │ e.g. "Display"
                                     │    value       VARCHAR   │ e.g. "6.1 OLED"
                                     └──────────────────────────┘

Relationships:
  users     → reviews       (OneToMany)
  products  → reviews       (OneToMany)
  products  → specifications (OneToMany)
```

---

## 📁 Project Structure

```
src/main/java/com/example/ProductComparison/
├── Config/
│   ├── OpenApiConfig.java        → Swagger / OpenAPI 3.0 setup
│   ├── RedisConfig.java          → Redis connection & serialisation
│   └── SecurityConfig.java       → JWT filter chain, RBAC rules
├── Controller/
│   ├── AuthController.java       → /auth/register, /auth/login
│   ├── ProductController.java    → /products (CRUD, search, pagination)
│   ├── ReviewController.java     → /products/{id}/reviews
│   ├── SpecificationController.java → /products/{id}/specifications
│   └── ComparisonController.java → /compare?productIds=1,2,3
├── Service/
│   ├── AuthService.java
│   ├── ProductService.java       → Redis cache-aside logic lives here
│   ├── ReviewService.java
│   ├── SpecificationService.java
│   └── ComparisonService.java    → spec attribute alignment logic
├── Repository/
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── ReviewRepository.java
│   └── SpecificationRepository.java
├── Model/
│   ├── User.java
│   ├── Product.java
│   ├── Review.java
│   └── Specification.java
├── Dto/
│   ├── request/                  → LoginRequest, RegisterRequest, etc.
│   └── response/                 → AuthResponse, ProductResponse, etc.
├── Exception/
│   ├── GlobalExceptionHandler.java  → @ControllerAdvice
│   └── ResourceNotFoundException.java
└── Security/
    ├── JwtUtil.java              → token generation & validation
    └── JwtAuthFilter.java        → OncePerRequestFilter
```

---

## 🔑 API Endpoints Summary

### Auth
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/auth/register` | Public | Register new USER |
| POST | `/auth/register/admin` | Public | Register ADMIN |
| POST | `/auth/login` | Public | Login, returns JWT token |

### Products
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/products` | Public | Get all (paginated + filtered) |
| GET | `/products/search?keyword=` | Public | Keyword search (paginated) |
| GET | `/products/{id}` | Public | Get by ID with specs & reviews |
| POST | `/products/add` | ADMIN | Add single product |
| POST | `/products/bulkadd` | ADMIN | Bulk add products |
| DELETE | `/products/{id}` | ADMIN | Soft delete |
| DELETE | `/products/{id}/hard` | ADMIN | Hard delete |

### Reviews
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/products/{id}/reviews` | USER | Add review |
| GET | `/products/{id}/reviews` | Public | Get all reviews |
| PUT | `/products/{id}/reviews` | USER | Update own review |
| DELETE | `/products/{id}/reviews` | USER | Delete own review |

### Specifications
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/products/{id}/specifications` | ADMIN | Add specs (bulk) |
| GET | `/products/{id}/specifications` | Public | Get specs |

### Comparison
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/compare?productIds=1,2,3` | Public | Compare 2–4 products |

---

## ⚙️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.3 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Cache | Redis (spring-boot-starter-data-redis) |
| Validation | Spring Boot Starter Validation |
| API Docs | Springdoc OpenAPI 2.8.8 (Swagger UI) |
| Utilities | Lombok |
| Build | Maven |
| Testing | JUnit + Spring Security Test |

---

## 🛠️ How to Run Locally

### Prerequisites
- Java 21+
- PostgreSQL running locally
- Redis running locally (`redis-server`)
- Maven

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/Gupendra-verma/product-comparison-api.git
cd product-comparison-api

# 2. Configure application.properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/product_comparison_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update

spring.redis.host=localhost
spring.redis.port=6379

jwt.secret=your-256-bit-secret-key
jwt.expiration=86400000

# 3. Create the database
psql -U postgres
CREATE DATABASE product_comparison_db;

# 4. Run the app
mvn spring-boot:run

# 5. Access Swagger UI
http://localhost:8080/swagger-ui.html

# 6. Get OpenAPI spec
http://localhost:8080/v3/api-docs
```

### Quick Test Flow
```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Use returned token for protected endpoints
curl -X GET http://localhost:8080/products \
  -H "Authorization: Bearer <your_token>"

# Compare products
curl -X GET "http://localhost:8080/compare?productIds=1,2,3"
```

---

## 👤 Author

**Gupendra Kumar**
[GitHub](https://github.com/Gupendra-verma) · [LinkedIn](https://linkedin.com/in/gupendraverma/)
