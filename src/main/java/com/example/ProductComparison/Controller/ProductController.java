package com.example.ProductComparison.Controller;

import com.example.ProductComparison.DTO.*;
import com.example.ProductComparison.Service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Controller
 * 
 * Manages all product-related operations including retrieval, creation,
 * updating, and deletion. Includes support for pagination, filtering,
 * and search functionality.
 */
@RestController
@Validated
@RequestMapping("/products")
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/check")
    @Operation(summary = "Health check", description = "Simple endpoint to verify the product service is running")
    public String getProducts() {
         return "This is a list of products";
    }

    @GetMapping
    @Operation(summary = "Get all products", 
               description = "Retrieve a paginated list of all products with optional filtering and sorting. " +
                           "Supports filtering by price range, brand, and category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or filter parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PagedResponseDto<ProductPageResponseDto>>getAllProducts(
            @Parameter(description = "Zero-based page number") 
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be 0 or more") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(defaultValue = "10") @Positive(message = "size must be greater than 0") int size,
            @Parameter(description = "Field to sort by (e.g., 'id', 'price', 'name')")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: 'asc' for ascending, 'desc' for descending")
            @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Minimum price filter (optional)")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter (optional)")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Brand filter (optional)")
            @RequestParam(required = false) String brand,
            @Parameter(description = "Category name filter (optional)")
            @RequestParam(required = false) String categoryName
    ) {
         return ResponseEntity.ok(productService.getAllProductsPagedResponse(page, size, sortBy, sortDir, minPrice, maxPrice, brand, categoryName));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name", 
               description = "Search for products by keyword with pagination and sorting support.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PagedResponseDto<ProductPageResponseDto>> searchProductsByName(
            @Parameter(description = "Search keyword (minimum 1 character)")
            @RequestParam @Size(min = 1, message = "keyword must not be empty") String keyword,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be 0 or more") int page,
            @Parameter(description = "Number of records per page")
            @RequestParam(defaultValue = "10") @Positive(message = "size must be greater than 0") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: 'asc' or 'desc'")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(productService.searchProductsByNamePagedResponse(keyword, page, size, sortBy, sortDir));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Retrieve detailed information about a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProductResponseDto> getProductById(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId) {

        return ResponseEntity.ok(productService.getProductById(productId));

    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new product", 
               description = "Create a new product. Only accessible to administrators.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - admin access required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> addProduct(@Valid @RequestBody ProductRequestDto productRequestDto) {
         productService.addProduct(productRequestDto);
        return ResponseEntity.ok("product with name " + productRequestDto.getName() + " has been added successfully");

    }

    @PostMapping("/bulkadd")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk add products", 
               description = "Create multiple products at once. Only accessible to administrators.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data in the list"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - admin access required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BulkProductResponseDto> bulkAddProduct(@Valid @RequestBody List<ProductRequestDto> listOfProduct){

       return ResponseEntity.ok( productService.bulkAddProduct(listOfProduct));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product (soft delete)", 
               description = "Mark a product as deleted without removing it from the database. Only accessible to administrators.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - admin access required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> deleteProduct(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId) {
         productService.softDeleteProduct(productId);
         return ResponseEntity.ok("Product with "+productId+ " has been hard deleted");
    }

    @DeleteMapping("/{productId}/hard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Permanently delete product (hard delete)", 
               description = "Permanently remove a product from the database. This action cannot be undone. Only accessible to administrators.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product permanently deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - admin access required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> hardDeleteProduct(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId) {
        productService.hardDeleteProduct(productId);
        return ResponseEntity.ok("product with id " + productId + " has been permanently deleted");
    }

}
