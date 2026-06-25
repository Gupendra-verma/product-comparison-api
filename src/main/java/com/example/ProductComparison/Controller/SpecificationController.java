package com.example.ProductComparison.Controller;

import com.example.ProductComparison.DTO.SpecificationRequestDto;
import com.example.ProductComparison.DTO.SpecificationResponseDto;
import com.example.ProductComparison.Service.SpecificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Specification Controller
 * 
 * Manages product specifications such as technical details, features,
 * dimensions, and other product attributes.
 */
@RestController
@RequestMapping("/products/{productId}/specifications")
@Tag(name = "Specifications", description = "Product specifications management endpoints")
public class SpecificationController {
    @Autowired
    SpecificationService specificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add product specifications", 
               description = "Add one or more specifications to a product. Only administrators can add specifications.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specifications added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid specification data"),
            @ApiResponse(responseCode = "403", description = "Unauthorized - admin access required"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void addSpecification(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId,
            @Parameter(description = "List of specifications to add")
            @RequestBody @NotEmpty(message = "Specifications list cannot be empty")
            List<@Valid SpecificationRequestDto> specs) {
        specificationService.addSpecification(productId, specs);
    }

    @GetMapping
    @Operation(summary = "Get product specifications", 
               description = "Retrieve all specifications for a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specifications retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<SpecificationResponseDto> getSpecifications(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId) {
        return specificationService.getSpecificationsByProductId(productId);
    }
}
