package com.example.ProductComparison.Mapper;

import com.example.ProductComparison.DTO.ProductRequestDto;
import com.example.ProductComparison.DTO.ProductResponseDto;
import com.example.ProductComparison.DTO.ProductPageResponseDto;
import com.example.ProductComparison.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Mapper for Product entity to DTO conversions.
 * Handles conversion from Product entity to various DTOs.
 */
@Component
public class ProductMapper {

    /**
     * Convert Product entity to ProductListDto
     * 
     * @param product the Product entity
     * @return ProductListDto
     */
    public ProductResponseDto toProductResponseDto(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .rating(product.getRating())
                .totalReviews(product.getTotalReviews())
                .build();
    }

    public Product toEntity(ProductRequestDto dto) {

        Product product = new Product();

        product.setName(dto.getName().trim());
        product.setBrand(dto.getBrand().trim());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());

        return product;
    }


    /**
     * Convert a Page of Product entities to ProductPageResponseDto
     * 
     * @param page the Page containing ProductListDto items
     * @return ProductPageResponseDto with pagination details
     */
    public ProductPageResponseDto toProductPageResponseDto(Page<ProductResponseDto> page) {
        if (page == null) {
            return null;
        }

        return ProductPageResponseDto.builder()
                .products(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
