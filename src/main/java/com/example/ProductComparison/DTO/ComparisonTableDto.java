package com.example.ProductComparison.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Product Comparison Table DTO
 * 
 * Contains comparison data in a tabular format showing products
 * and their specification values side-by-side
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Comparison table with products and their specifications")
public class ComparisonTableDto {
    @Schema(description = "List of products being compared")
    private List<ComparedProductDto> products;
    
    @Schema(description = "Comparison rows with attributes and values for each product")
    private List<ComparisonRowDto> rows;
}

