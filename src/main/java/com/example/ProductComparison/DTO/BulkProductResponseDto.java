package com.example.ProductComparison.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class BulkProductResponseDto {
    int totalReceived;
    int inserted;
    int totalSkipped;
    List<String> skippedProducts;
}
