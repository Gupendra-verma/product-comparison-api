package com.example.ProductComparison.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Authentication Response DTO
 * 
 * Contains the JWT token and user information after successful authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response with JWT token and user details")
public class AuthResponseDto {
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;
    
    @Schema(description = "Authenticated username", example = "john_doe")
    private String username;
    
    @Schema(description = "User role", example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;
}

