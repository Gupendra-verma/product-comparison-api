package com.example.ProductComparison.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews",
uniqueConstraints = {
    @UniqueConstraint(
            name = "uk_user_product_review",
            columnNames = {"user_id", "product_id"}
    )
},
indexes = {
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_user_product", columnList = "user_id,product_id")
})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    private Double rating;
    private String comment;
    @CreationTimestamp
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;
}
