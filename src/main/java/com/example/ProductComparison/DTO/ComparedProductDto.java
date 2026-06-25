package com.example.ProductComparison.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Compared Product DTO
 * 
 * Basic information about a product in a comparison
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product information in comparison view")
public class ComparedProductDto {
    @Schema(description = "Product ID", example = "1")
    private Long id;
    
    @Schema(description = "Product name", example = "iPhone 14 Pro")
    private String name;
    
    @Schema(description = "Product brand", example = "Apple")
    private String brand;
}

