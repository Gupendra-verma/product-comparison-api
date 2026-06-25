package com.example.ProductComparison.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpecificationRequestDto {
    @NotBlank(message = "Specification type is required")
    private String specType;
    @NotBlank(message = "Specification value is required")
    private String value;
}
