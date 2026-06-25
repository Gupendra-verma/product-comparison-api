package com.example.ProductComparison.Repository;

import com.example.ProductComparison.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category, Long> {
     Category findByName(String name);
}
