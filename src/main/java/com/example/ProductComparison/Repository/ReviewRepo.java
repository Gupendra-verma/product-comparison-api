package com.example.ProductComparison.Repository;

import com.example.ProductComparison.Entity.AppUser;
import com.example.ProductComparison.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepo extends JpaRepository<Review, Long> {
     List<Review> findByProductId(Long productId);
     Optional<Review> findByProductIdAndUserId (Long productId, Long userId);

     @Query("""
       SELECT AVG(r.rating)
       FROM Review r
       WHERE r.product.id = :productId
       """)
     Double calculateAverageRating(Long productId);

     int countByProductId(Long productId);
     boolean existsByUserIdAndProductId(Long userId, Long productId);
}

