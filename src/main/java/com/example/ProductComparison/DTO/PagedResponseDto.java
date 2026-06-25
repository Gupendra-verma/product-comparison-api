package com.example.ProductComparison.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic Paginated Response DTO
 * 
 * Wraps paginated data responses with metadata
 * 
 * @param <T> The type of data contained in this response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Generic paginated response wrapper", title = "PagedResponse")
public class PagedResponseDto<T> {
    @Schema(description = "Response message", example = "Products retrieved successfully")
    private String message;
    
    @Schema(description = "Paginated data content")
    private T data;
}

