package com.example.ProductComparison.Controller;

import com.example.ProductComparison.DTO.ReviewRequestDto;
import com.example.ProductComparison.DTO.ReviewResponseDto;
import com.example.ProductComparison.Service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import java.time.Instant;
import java.util.Map;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Review Controller
 * 
 * Manages product reviews including creation, retrieval, updating, and deletion.
 * Only authenticated users can create or modify reviews.
 */
@RestController
@RequestMapping("/products/{productId}/reviews")
@Tag(name = "Reviews", description = "Product review management endpoints")
public class ReviewController {
    @Autowired
    ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Add product review", 
               description = "Create a new review for a product. Only authenticated users can add reviews.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid review data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user must be logged in"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Review already exists for this product"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> addReview(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId, 
            @Valid @RequestBody ReviewRequestDto reviewRequestDto) {
        reviewService.addReview(productId, reviewRequestDto);
        return ResponseEntity.status(201).body(Map.of(
                "message", "Review added successfully",
                "productId", productId,
                "timestamp", Instant.now()
        ));
    }

    @GetMapping
    @Operation(summary = "Get product reviews", description = "Retrieve all reviews for a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<ReviewResponseDto> getReviews(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId) {
        return reviewService.getReviewsByProductId(productId);
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete review", 
               description = "Delete a review. Only the review owner or admin can delete.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user must be logged in"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot delete this review"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> deleteReview(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId){
        reviewService.deleteReview(productId);
        return ResponseEntity.ok(Map.of(
                "message", "Review deleted successfully",
                "productId", productId,
                "timestamp", Instant.now()
        ));
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update review", 
               description = "Update an existing review. Only the review owner or admin can update.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid review data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user must be logged in"),
            @ApiResponse(responseCode = "403", description = "Forbidden - cannot update this review"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ReviewResponseDto> updateReview(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId, 
            @Valid @RequestBody ReviewRequestDto reviewRequestDto){
        ReviewResponseDto updated = reviewService.updateReview(productId, reviewRequestDto);
        return ResponseEntity.ok(updated);
    }
}
