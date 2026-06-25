package com.example.ProductComparison.Controller;

import com.example.ProductComparison.DTO.AuthAdminRegisterRequestDto;
import com.example.ProductComparison.DTO.AuthLoginRequestDto;
import com.example.ProductComparison.DTO.AuthRegisterRequestDto;
import com.example.ProductComparison.DTO.AuthResponseDto;
import com.example.ProductComparison.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Authentication Controller
 * 
 * Handles user authentication operations including registration and login.
 * All endpoints return JWT tokens for subsequent authenticated requests.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", 
               description = "Create a new user account with email and password. " +
                           "Returns a JWT token that can be used for subsequent authenticated requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public AuthResponseDto register(@Valid @RequestBody AuthRegisterRequestDto req) {
        return authService.register(req);
    }

    @PostMapping("/register/admin")
    @Operation(summary = "Register new admin user", 
               description = "Create a new admin account with email and password. " +
                           "This endpoint is typically restricted to administrators only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin user registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - insufficient permissions"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public AuthResponseDto registerAdmin(@Valid @RequestBody AuthAdminRegisterRequestDto req) {
        return authService.registerAdmin(req);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", 
               description = "Authenticate user with email and password. " +
                           "Returns a JWT token that must be included in the Authorization header for subsequent requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or malformed request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid email or password"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public AuthResponseDto login(@Valid @RequestBody AuthLoginRequestDto req) {
        return authService.login(req);
    }
}

