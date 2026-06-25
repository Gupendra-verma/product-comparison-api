package com.example.ProductComparison.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user attempts to create a duplicate review
 * A user can only have one review per product
 */
public class DuplicateReviewException extends DomainException {
    
    private final Long userId;
    private final Long productId;
    
    public DuplicateReviewException(Long userId, Long productId) {
        super("User " + userId + " has already reviewed product " + productId +
              ". Only one review per product per user is allowed.");
        this.userId = userId;
        this.productId = productId;
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public Long getProductId() {
        return productId;
    }
}
