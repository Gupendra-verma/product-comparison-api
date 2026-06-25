package com.example.ProductComparison.Repository;

import com.example.ProductComparison.Entity.SpecType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecsTypeRepo extends JpaRepository<SpecType, Long> {
    SpecType findByName(String name);
}
