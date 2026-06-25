# Swagger Documentation - Developer Guide

## Overview

This guide is for developers who need to maintain, update, or customize the Swagger API documentation for the Product Comparison platform.

---

## Table of Contents

1. [Adding Swagger Annotations](#adding-swagger-annotations)
2. [Customizing Swagger UI](#customizing-swagger-ui)
3. [Updating Documentation](#updating-documentation)
4. [Common Annotation Patterns](#common-annotation-patterns)
5. [Troubleshooting](#troubleshooting)
6. [Advanced Configuration](#advanced-configuration)

---

## Adding Swagger Annotations

### For New Controller Classes

```java
package com.example.ProductComparison.Controller;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.*;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.*;

/**
 * Brief description of what this controller does.
 * Include any important business logic notes.
 */
@RestController
@RequestMapping("/your-endpoint")
@Tag(name = "Feature Name", description = "Detailed description of what this feature provides")
public class YourNewController {

    /**
     * Briefly describe what this method does
     */
    @GetMapping
    @Operation(summary = "One-line summary", 
               description = "Detailed description explaining:\n" +
                           "- What the endpoint does\n" +
                           "- What it returns\n" +
                           "- Any special behavior")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success message",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = YourResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public YourResponseDto yourMethod(
            @Parameter(description = "Parameter description")
            @RequestParam String param1,
            @Parameter(description = "Another parameter")
            @PathVariable Long id) {
        return new YourResponseDto();
    }

    /**
     * Protected endpoint requiring authentication
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Admin-only operation")
    public void adminOperation(@Valid @RequestBody YourRequestDto request) {
        // Implementation
    }
}
```

### For New DTO Classes

```java
package com.example.ProductComparison.DTO;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Purpose and usage of this DTO.
 * Include any validation rules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Clear description of what this DTO represents",
        example = "{\"id\": 1, \"name\": \"Example\"}")
public class YourNewDto {
    
    @Schema(description = "Field description",
            example = "123",
            minimum = "1",
            maximum = "1000")
    private Long id;
    
    @Schema(description = "Name field description",
            example = "Product Name",
            minLength = 1,
            maxLength = 100,
            required = true)
    private String name;
    
    @Schema(description = "Numeric field with constraints",
            example = "99.99",
            minimum = "0",
            exclusiveMinimum = true)
    private BigDecimal price;
    
    @Schema(description = "Enum example",
            allowableValues = {"ACTIVE", "INACTIVE", "DELETED"},
            example = "ACTIVE")
    private String status;
}
```

---

## Customizing Swagger UI

### UI Configuration in application.properties

```properties
# === Swagger UI Customization ===

# Basic paths and settings
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# UI Layout and Behavior
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.doc-expansion=list           # Can be: list, full, none
springdoc.swagger-ui.filter=true                  # Show/hide filter search
springdoc.swagger-ui.show-extensions=true
springdoc.swagger-ui.operations-sorter=method     # Sort endpoints: method, alpha
springdoc.swagger-ui.tags-sorter=alpha            # Sort tags alphabetically

# Visual Settings
springdoc.swagger-ui.syntax-highlight=monokai     # Code highlight theme
springdoc.swagger-ui.use-root-path=true
springdoc.swagger-ui.url=/v3/api-docs

# Disable in Production (Optional)
# springdoc.swagger-ui.enabled=false

# API Filtering (Only show specific packages/paths)
springdoc.paths-to-match=/auth/**,/products/**,/compare/**
springdoc.packages-to-scan=com.example.ProductComparison.Controller
springdoc.show-actuator=false
```

### Custom Swagger UI HTML

To customize the Swagger UI appearance, create:
```
src/main/resources/swagger-ui/index.html
src/main/resources/swagger-ui/config.js
```

---

## Updating Documentation

### Adding a New Endpoint

1. **Create the controller method with annotations:**
   ```java
   @PostMapping("/new-feature")
   @Operation(summary = "New feature", description = "Does something new")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Success"),
       @ApiResponse(responseCode = "400", description = "Invalid input")
   })
   public ResponseDto newFeature(@Valid @RequestBody RequestDto request) {
       // Implementation
   }
   ```

2. **Test in Swagger UI:**
   - Navigate to `http://localhost:8091/swagger-ui.html`
   - Refresh the page
   - Your new endpoint should appear

3. **Update documentation files:**
   - Add entry to `API_DOCUMENTATION.md`
   - Update `SWAGGER_QUICK_START.md` if user-facing
   - Update `SWAGGER_SETUP_SUMMARY.md` endpoint count

### Adding a New Tag (Feature Group)

```java
@RestController
@RequestMapping("/your-feature")
@Tag(name = "Feature Name", 
     description = "Comprehensive description of what this feature does")
public class YourController {
    // Methods here
}
```

### Updating Existing Endpoint Documentation

```java
// Before:
@GetMapping
public List<ProductDto> getProducts() {
    return productService.getAllProducts();
}

// After:
@GetMapping
@Operation(summary = "Get all products",
           description = "Retrieve a complete list of all available products. " +
                       "No pagination in this simplified endpoint.")
@ApiResponse(responseCode = "200", description = "List of products retrieved")
public List<ProductDto> getProducts() {
    return productService.getAllProducts();
}
```

---

## Common Annotation Patterns

### Pattern 1: Simple GET Endpoint

```java
@GetMapping("/{id}")
@Operation(summary = "Get by ID", description = "Retrieve a resource by its ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Resource found"),
    @ApiResponse(responseCode = "404", description = "Resource not found")
})
public ResponseDto getById(
    @Parameter(description = "Resource ID") 
    @PathVariable Long id) {
    // Implementation
}
```

### Pattern 2: Paginated GET Endpoint

```java
@GetMapping
@Operation(summary = "List with pagination")
public PagedResponseDto<ItemDto> getAll(
    @Parameter(description = "Page number (0-based)")
    @RequestParam(defaultValue = "0") @Min(0) int page,
    
    @Parameter(description = "Items per page")
    @RequestParam(defaultValue = "10") @Positive int size,
    
    @Parameter(description = "Sort field")
    @RequestParam(defaultValue = "id") String sortBy,
    
    @Parameter(description = "asc or desc")
    @RequestParam(defaultValue = "asc") String sortDir) {
    // Implementation
}
```

### Pattern 3: Protected POST Endpoint

```java
@PostMapping
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Operation(summary = "Create resource (admin only)")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input"),
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
})
public ResponseDto create(
    @Valid @RequestBody CreateRequestDto request) {
    // Implementation
}
```

### Pattern 4: DELETE Endpoint

```java
@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Operation(summary = "Delete resource")
@ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Not found"),
    @ApiResponse(responseCode = "403", description = "Cannot delete")
})
public ResponseEntity<Void> delete(
    @Parameter(description = "Resource ID")
    @PathVariable Long id) {
    // Implementation
    return ResponseEntity.noContent().build();
}
```

### Pattern 5: Complex Request with Multiple Parameters

```java
@PostMapping("/search")
@Operation(summary = "Advanced search",
           description = "Search with multiple filter criteria")
public PagedResponseDto<ResultDto> search(
    @Parameter(description = "Search keyword")
    @RequestParam String keyword,
    
    @Parameter(description = "Minimum price")
    @RequestParam(required = false) BigDecimal minPrice,
    
    @Parameter(description = "Maximum price")
    @RequestParam(required = false) BigDecimal maxPrice,
    
    @Parameter(description = "Category names", example = "Electronics, Fashion")
    @RequestParam(required = false) List<String> categories,
    
    @Parameter(description = "Page number")
    @RequestParam(defaultValue = "0") int page,
    
    @Parameter(description = "Page size")
    @RequestParam(defaultValue = "20") int size) {
    // Implementation
}
```

---

## Troubleshooting

### Issue: Annotation changes not showing in Swagger UI

**Solution:**
1. Clean build cache: `mvn clean`
2. Rebuild: `mvn compile`
3. Hard refresh browser: `Ctrl+Shift+R` or `Cmd+Shift+R`
4. Restart application

### Issue: "Cannot resolve annotation" error

**Solution:**
- Verify Springdoc dependency is in pom.xml
- Run: `mvn dependency:resolve`
- Check IDE project configuration

### Issue: Complex types not showing correct schema

**Solution:**
```java
@Schema(implementation = YourComplexDto.class,
        description = "Detailed explanation")
public ResponseEntity<YourComplexDto> getComplex() {
    // Implementation
}
```

### Issue: Parameter validation rules not showing

**Solution:**
- Add constraints to method parameters:
```java
@RequestParam 
@NotBlank(message = "Field required")
@Size(min = 3, max = 50)
@Schema(minLength = 3, maxLength = 50, required = true)
String field
```

### Issue: Authorization not working in Swagger UI

**Solution:**
- Ensure `@SecurityRequirement(name = "Bearer Authentication")` is present
- Verify token format: `Bearer <token>`
- Check user role matches endpoint requirements

---

## Advanced Configuration

### Exclude Endpoints from Documentation

```java
// In OpenApiConfig.java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        // ... configuration ...
        .addServersItem(new Server()
            .url("http://localhost:8091")
            .description("Development server"))
        .addServersItem(new Server()
            .url("https://api.production.com")
            .description("Production server"));
}
```

### Add Server Configuration

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .servers(Arrays.asList(
            new Server()
                .url("http://localhost:8091")
                .description("Development"),
            new Server()
                .url("https://api.example.com")
                .description("Production")
        ))
        // ... rest of configuration
}
```

### Custom Security Scheme

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("api_key", 
                new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("X-API-Key")))
        .addSecurityItem(new SecurityRequirement()
            .addList("api_key"));
}
```

### Generate Client SDK

Using Swagger/OpenAPI codegen:

```bash
# JavaScript/TypeScript
swagger-codegen generate -i http://localhost:8091/v3/api-docs \
  -l javascript \
  -o ./generated/js-client

# Python
swagger-codegen generate -i http://localhost:8091/v3/api-docs \
  -l python \
  -o ./generated/python-client

# Java
swagger-codegen generate -i http://localhost:8091/v3/api-docs \
  -l java \
  -o ./generated/java-client
```

---

## Best Practices

### 1. Documentation Quality
- Always provide descriptions (don't skip)
- Use examples for clarity
- Document all error scenarios
- Explain why errors occur

### 2. Parameter Documentation
```java
// Good
@Parameter(description = "Product ID (unique identifier in database)")
Long productId

// Bad
@Parameter(description = "ID")
Long id
```

### 3. Response Documentation
```java
// Good
@ApiResponse(responseCode = "400", 
            description = "Invalid product data. Check that all required fields are present and valid.")
            
// Bad
@ApiResponse(responseCode = "400", 
            description = "Error")
```

### 4. Keep Examples Real
```java
// Good
@Schema(example = "iPhone 14 Pro")

// Bad
@Schema(example = "product")
```

### 5. Organize with Tags
```java
@Tag(name = "Product Management", 
     description = "CRUD operations for products")
```

---

## Maintenance Checklist

When modifying the API:

- [ ] Update method annotations
- [ ] Update DTO annotations
- [ ] Update response types in `@ApiResponse`
- [ ] Add new error codes to `@ApiResponses`
- [ ] Test in Swagger UI
- [ ] Update `API_DOCUMENTATION.md`
- [ ] Update `SWAGGER_QUICK_START.md` if user-facing
- [ ] Update code examples if signatures changed
- [ ] Test with cURL or Postman
- [ ] Check error handling documentation

---

## Resources

- [Springdoc-OpenAPI Documentation](https://springdoc.org/)
- [Swagger/OpenAPI Specification](https://swagger.io/specification/)
- [Jakarta Annotations](https://jakarta.ee/specifications/annotations/)
- [Spring Security](https://spring.io/projects/spring-security)

---

**This guide helps maintain API documentation quality.**
