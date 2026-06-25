package com.example.ProductComparison.Repository;

import com.example.ProductComparison.Entity.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecificationRepo extends JpaRepository<ProductSpecification, Long> {

    @Query("SELECT ps FROM ProductSpecification ps " +
            "JOIN FETCH ps.product p " +
            "JOIN FETCH ps.specType st " +
            "WHERE p.id IN :ids")
    List<ProductSpecification> findAllByProductIds(@Param("ids") List<Long> ids);

    List<ProductSpecification> findByProductId(Long productId);
}
