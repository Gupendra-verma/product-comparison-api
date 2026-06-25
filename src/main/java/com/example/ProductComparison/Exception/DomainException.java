package com.example.ProductComparison.Exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for domain-specific exceptions
 * Provides structured exception handling across the application
 */
public abstract class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Returns the appropriate HTTP status code for this exception
     */
    public abstract HttpStatus getHttpStatus();
}
