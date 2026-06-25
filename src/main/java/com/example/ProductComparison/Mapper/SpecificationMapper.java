package com.example.ProductComparison.Mapper;

import com.example.ProductComparison.DTO.SpecificationResponseDto;
import com.example.ProductComparison.Entity.ProductSpecification;
import com.example.ProductComparison.Entity.SpecType;
import org.springframework.stereotype.Component;

/**
 * Mapper for ProductSpecification entity to DTO conversions.
 * Handles conversion from ProductSpecification entity to SpecificationResponseDto.
 */
@Component
public class SpecificationMapper {

    /**
     * Convert ProductSpecification entity to SpecificationResponseDto
     * 
     * @param specification the ProductSpecification entity
     * @param specType the SpecType entity containing the specification type name
     * @return SpecificationResponseDto
     */
    public SpecificationResponseDto toSpecificationResponseDto(
            ProductSpecification specification,
            SpecType specType) {
        
        if (specification == null) {
            return null;
        }

        String specTypeName = specType != null ? specType.getName() : "Unknown";

        return SpecificationResponseDto.builder()
                .productId(specification.getProductId())
                .specType(specTypeName)
                .value(specification.getSpecValue())
                .build();
    }

    /**
     * Alternative method if SpecType name is already available
     * 
     * @param specification the ProductSpecification entity
     * @param specTypeName the specification type name
     * @return SpecificationResponseDto
     */
    public SpecificationResponseDto toSpecificationResponseDto(
            ProductSpecification specification,
            String specTypeName) {
        
        if (specification == null) {
            return null;
        }

        String typeName = specTypeName != null ? specTypeName : "Unknown";

        return SpecificationResponseDto.builder()
                .productId(specification.getProductId())
                .specType(typeName)
                .value(specification.getSpecValue())
                .build();
    }
}
