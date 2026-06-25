package com.example.ProductComparison.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * User Login Request DTO
 */
@Data
@Schema(description = "Request payload for user login", example = "{\"username\": \"john_doe\", \"password\": \"securePassword123\"}")
public class AuthLoginRequestDto {
    @NotBlank
    @Schema(description = "Username or email", example = "john_doe")
    private String username;

    @NotBlank
    @Schema(description = "User password", example = "securePassword123")
    private String password;
}

