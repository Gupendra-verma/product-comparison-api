package com.example.ProductComparison.Mapper;

import com.example.ProductComparison.DTO.ReviewResponseDto;
import com.example.ProductComparison.Entity.Review;
import org.springframework.stereotype.Component;

/**
 * Mapper for Review entity to DTO conversions.
 * Handles conversion from Review entity to ReviewResponseDto.
 */
@Component
public class ReviewMapper {

    /**
     * Convert Review entity to ReviewResponseDto
     * 
     * @param review the Review entity
     * @return ReviewResponseDto
     */
    public ReviewResponseDto toReviewResponseDto(Review review) {
        if (review == null) {
            return null;
        }

        String userName = review.getUser() != null ? review.getUser().getUsername() : "Unknown";

        return ReviewResponseDto.builder()
                .productId(review.getProductId())
                .rating(review.getRating())
                .comment(review.getComment())
                .userName(userName)
                .build();
    }
}
