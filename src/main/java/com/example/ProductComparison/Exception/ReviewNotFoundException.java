package com.example.ProductComparison.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user attempts to modify/delete a review they don't own
 */
public class ReviewNotFoundException extends DomainException {
    
    private final Long productId;
    private final Long userId;
    
    public ReviewNotFoundException(Long productId, Long userId) {
        super("No review found for product ID " + productId +
                " submitted by user ID " + userId + ".");
        this.productId = productId;
        this.userId = userId;
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public Long getUserId() {
        return userId;
    }
}
