package com.example.ProductComparison.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * User Registration Request DTO
 */
@Data
@Schema(description = "Request payload for user registration", example = "{\"username\": \"john_doe\", \"password\": \"securePassword123\"}")
public class AuthRegisterRequestDto {
    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Username for the account", example = "john_doe", minLength = 3, maxLength = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    @Schema(description = "Password for the account", example = "securePassword123", minLength = 6, maxLength = 100)
    private String password;
}

