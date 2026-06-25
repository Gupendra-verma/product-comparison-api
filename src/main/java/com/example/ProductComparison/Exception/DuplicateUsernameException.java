package com.example.ProductComparison.Exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to register with a username that already exists
 */
public class DuplicateUsernameException extends DomainException {
    
    private final String username;
    
    public DuplicateUsernameException(String username) {
        super("Username '" + username + "' already exists. Please choose a different username.");
        this.username = username;
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
    
    public String getUsername() {
        return username;
    }
}
