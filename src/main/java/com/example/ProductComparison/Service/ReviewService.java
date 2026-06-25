package com.example.ProductComparison.Service;

import com.example.ProductComparison.DTO.ReviewRequestDto;
import com.example.ProductComparison.DTO.ReviewResponseDto;
import com.example.ProductComparison.Entity.AppUser;
import com.example.ProductComparison.Entity.Product;
import com.example.ProductComparison.Entity.Review;
import com.example.ProductComparison.Exception.DuplicateReviewException;
import com.example.ProductComparison.Exception.ProductNotFoundException;
import com.example.ProductComparison.Exception.ReviewNotFoundException;
import com.example.ProductComparison.Mapper.ReviewMapper;
import com.example.ProductComparison.Repository.ProductRepo;
import com.example.ProductComparison.Repository.ReviewRepo;
import com.example.ProductComparison.Repository.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
public class ReviewService {
    @Autowired
    ReviewRepo reviewRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    ProductRepo productRepo;
    @Autowired
    AuthService auth;
    @Autowired
    ReviewMapper reviewMapper;

    @Caching(evict = {
            @CacheEvict(value = "productList", allEntries = true),
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "comparison", allEntries = true)})
    public void addReview(Long productId, ReviewRequestDto reviewRequestDto) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();
        AppUser user = userRepo.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (reviewRepo.existsByUserIdAndProductId(
                user.getId(),
                productId)) {

            throw new DuplicateReviewException(
                    user.getId(),
                    productId
            );
        }

        Review review = new Review();
        review.setProductId(productId);
        review.setRating(reviewRequestDto.getRating());
        review.setComment(reviewRequestDto.getComment());
        review.setUser(user);
        reviewRepo.save(review);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Double avgRating = reviewRepo.calculateAverageRating(productId);
        Integer totalReviews = reviewRepo.countByProductId(productId);
        double roundedRating = BigDecimal.valueOf(avgRating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        product.setRating(roundedRating);
        product.setTotalReviews(totalReviews);

        productRepo.save(product);
    }

    public List<ReviewResponseDto> getReviewsByProductId(Long productId) {
        List<Review> reviews = reviewRepo.findByProductId(productId);
        return reviews.stream()
                .map(reviewMapper::toReviewResponseDto)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "productList", allEntries = true),
            @CacheEvict(value = "comparison", allEntries = true)
    })
    public void deleteReview(Long productId) {

        AppUser user = auth.getCurrentUser();

        Review review = reviewRepo
                .findByProductIdAndUserId(productId, user.getId())
                .orElseThrow(() ->
                        new ReviewNotFoundException(productId, user.getId()));

        reviewRepo.delete(review);
        updateProductRating(productId);

    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "productList", allEntries = true),
            @CacheEvict(value = "comparison", allEntries = true)
    })
    public ReviewResponseDto updateReview(Long productId, ReviewRequestDto reviewRequestDto){
        AppUser user = auth.getCurrentUser();
        Review review = reviewRepo.findByProductIdAndUserId(productId,user.getId())
                .orElseThrow(() ->
                new ReviewNotFoundException(productId, user.getId()));

        review.setRating(reviewRequestDto.getRating());
        review.setComment(reviewRequestDto.getComment());

        reviewRepo.save(review);

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Double avgRating = reviewRepo.calculateAverageRating(productId);
        if (avgRating == null) {
            product.setRating(0.0);
            product.setTotalReviews(0);
        } else {
            double roundedRating = BigDecimal.valueOf(avgRating)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();

            product.setRating(roundedRating);
            product.setTotalReviews(reviewRepo.countByProductId(productId));
        }

        productRepo.save(product);
        return reviewMapper.toReviewResponseDto(review);

    }

    private void updateProductRating(Long productId) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId));

        int totalReviews = reviewRepo.countByProductId(productId);
        Double avgRating = reviewRepo.calculateAverageRating(productId);
        double roundedRating = BigDecimal.valueOf(avgRating)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        product.setRating(roundedRating);
        product.setTotalReviews(totalReviews);
        productRepo.save(product);
    }
}
