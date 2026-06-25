package com.example.ProductComparison.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Product Page Response DTO
 * 
 * Contains paginated product list along with pagination metadata
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Paginated product list response")
public class ProductPageResponseDto {
    @Schema(description = "List of products in this page")
    private List<ProductResponseDto> products;
    
    @Schema(description = "Current page number (zero-based)", example = "0")
    private int page;
    
    @Schema(description = "Number of records in this page", example = "10")
    private int size;
    
    @Schema(description = "Total number of products", example = "100")
    private long totalElements;
    
    @Schema(description = "Total number of pages", example = "10")
    private int totalPages;
    
    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;
}
