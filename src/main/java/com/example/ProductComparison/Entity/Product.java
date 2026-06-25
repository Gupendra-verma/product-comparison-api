package com.example.ProductComparison.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Data
@Table(name = "products", indexes = {
    @Index(name = "idx_brand", columnList = "brand"),
    @Index(name = "idx_price", columnList = "price"),
    @Index(name = "idx_category_id", columnList = "categoryId"),
    @Index(name = "idx_deleted", columnList = "deleted"),
    @Index(name = "idx_price_brand", columnList = "price,brand"),
    @Index(name = "idx_category_deleted", columnList = "categoryId,deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE products SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private Long categoryId;
    private Double rating;
    private String description;
    private Integer totalReviews;
    private Boolean deleted = false;
    @CreationTimestamp
    private Timestamp createdAt;
    @UpdateTimestamp
    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId", insertable = false, updatable = false)
    private Category category;

}
