package com.example.ProductComparison.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Product Review Response DTO
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Product review details")
public class ReviewResponseDto {
    @Schema(description = "Product ID", example = "1")
    private Long productId;
    
    @Schema(description = "Username who posted the review", example = "john_doe")
    private String userName;
    
    @Schema(description = "Review comment or text", example = "Great product, very satisfied with the quality!")
    private String comment;
    
    @Schema(description = "Review rating (1-5 scale)", example = "4.5", minimum = "1", maximum = "5")
    private Double rating;
}
