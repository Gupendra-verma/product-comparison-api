package com.example.ProductComparison.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested product is not found
 */
public class ProductNotFoundException extends DomainException {
    
    private final Long productId;
    
    public ProductNotFoundException(Long productId) {
        super("Product with ID " + productId + " not found");
        this.productId = productId;
    }
    
    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    
    public Long getProductId() {
        return productId;
    }
}
