package com.example.ProductComparison.Repository;

import com.example.ProductComparison.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByNameContainingIgnoreCase(String keyword);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);


    @Query(value = "SELECT * FROM products WHERE id = :id", nativeQuery = true)
    Optional<Product> findByIdIncludingDeleted(@Param("id") Long id);

    boolean existsByNameIgnoreCaseAndBrandIgnoreCaseAndDeletedFalse(
            String name,
            String brand
    );

}
