package com.example.ProductComparison.Controller;

import com.example.ProductComparison.DTO.ComparisonTableDto;
import com.example.ProductComparison.Service.ProductComparisonService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Product Comparison Controller
 * 
 * Handles side-by-side product comparison. Allows users to compare
 * 2-4 products simultaneously to help with purchase decisions.
 */
@RestController
@RequestMapping("/compare")
@Tag(name = "Product Comparison", description = "Product comparison endpoints")
public class ProductComparisonController {
    private final ProductComparisonService comparisonService;

    @Autowired
    public ProductComparisonController(ProductComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping
    @Operation(summary = "Compare products", 
               description = "Compare 2 to 4 products side-by-side. Returns specifications and attributes " +
                           "in a tabular format for easy comparison.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comparison table generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ComparisonTableDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid product IDs or wrong number of products (must be 2-4)"),
            @ApiResponse(responseCode = "404", description = "One or more products not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ComparisonTableDto compareProducts(
            @Parameter(description = "List of 2-4 product IDs to compare", example = "[1, 2, 3]")
            @RequestParam
            @Size(min = 2, max = 4, message = "Compare requires between 2 and 4 product IDs")
            List<@NotNull(message = "Product ID cannot be null") Long> productIds
    ) {
        return comparisonService.compareProducts(productIds);
    }
}
