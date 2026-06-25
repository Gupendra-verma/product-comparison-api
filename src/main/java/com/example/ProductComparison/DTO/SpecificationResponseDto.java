package com.example.ProductComparison.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpecificationResponseDto {
    private Long productId;
    private String specType;
    private String value;

}
