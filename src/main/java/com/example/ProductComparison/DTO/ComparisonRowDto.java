package com.example.ProductComparison.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Comparison Row DTO
 * 
 * Represents a single row in the comparison table
 * showing an attribute and its values for all compared products
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Single row in comparison table with attribute and product values")
public class ComparisonRowDto {
    @Schema(description = "Attribute name (e.g., 'Color', 'Weight', 'Battery Life')", example = "Color")
    private String attribute;
    
    @Schema(description = "Values of this attribute for each product in the same order")
    private List<String> values;
}

