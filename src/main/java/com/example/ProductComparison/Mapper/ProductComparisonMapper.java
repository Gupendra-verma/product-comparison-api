package com.example.ProductComparison.Mapper;

import com.example.ProductComparison.DTO.ComparedProductDto;
import com.example.ProductComparison.DTO.ComparisonRowDto;
import com.example.ProductComparison.Entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for Product comparison-related DTO conversions.
 * Handles conversion for comparison table display.
 */
@Component
public class ProductComparisonMapper {

    /**
     * Convert Product entity to ComparedProductDto
     * Used for displaying basic product info in comparison table
     * 
     * @param product the Product entity
     * @return ComparedProductDto
     */
    public static ComparedProductDto toComparedProductDto(Product product) {
        if (product == null) {
            return null;
        }

        return ComparedProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .build();
    }

    /**
     * Convert a list of specification values to ComparisonRowDto
     * 
     * @param attribute the specification attribute name
     * @param values list of values for this attribute across compared products
     * @return ComparisonRowDto
     */
    public ComparisonRowDto toComparisonRowDto(String attribute, List<String> values) {
        if (attribute == null) {
            return null;
        }

        return ComparisonRowDto.builder()
                .attribute(attribute)
                .values(values != null ? values : List.of())
                .build();
    }
}
