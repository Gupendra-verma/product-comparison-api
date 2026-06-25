package com.example.ProductComparison.Service;

import com.example.ProductComparison.DTO.SpecificationRequestDto;
import com.example.ProductComparison.DTO.SpecificationResponseDto;
import com.example.ProductComparison.Entity.ProductSpecification;
import com.example.ProductComparison.Entity.SpecType;
import com.example.ProductComparison.Mapper.SpecificationMapper;
import com.example.ProductComparison.Repository.SpecificationRepo;
import com.example.ProductComparison.Repository.SpecsTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    SpecificationRepo specsRepo;
    @Autowired
    SpecsTypeRepo specsTypeRepo;
    @Autowired
    SpecificationMapper specificationMapper;


    @Caching(evict = {
            @CacheEvict(value = "specifications", key = "#productId"),
            @CacheEvict(value = "comparison", allEntries = true)
    })
    public void addSpecification(Long productId, List<SpecificationRequestDto> specs) {
            for(SpecificationRequestDto specDto : specs) {
                if(specDto==null) {
                    throw new IllegalArgumentException("Specification cannot be null");

                }

                SpecType specType = specsTypeRepo.findByName(specDto.getSpecType());
                if (specType == null) {
                    specType = new SpecType();
                    specType.setName(specDto.getSpecType());
                    specType = specsTypeRepo.save(specType);
                }
                ProductSpecification specification = new ProductSpecification();
                specification.setProductId(productId);
                specification.setSpecTypeId(specType.getId());
                specification.setSpecValue(specDto.getValue());
                specsRepo.save(specification);

            }

        }

    @Cacheable(value = "specifications", key = "#productId")
    public List<SpecificationResponseDto> getSpecificationsByProductId(Long productId) {
       List<ProductSpecification> specs = specsRepo.findByProductId(productId);
         if(specs.isEmpty()) {
              throw new IllegalArgumentException("No specifications found for product with id " + productId);
         }
            return specs.stream()
                    .map(s -> {
                        SpecType specType = specsTypeRepo.findById(s.getSpecTypeId())
                                .orElseThrow(() -> new IllegalArgumentException("Spec type not found for id " + s.getSpecTypeId()));
                        return specificationMapper.toSpecificationResponseDto(s,specType);
                    })
                    .toList();


    }
    public void deleteSpecificationsByProductId(Long productId) {
        List<ProductSpecification> specs = specsRepo.findByProductId(productId);
        if (specs.isEmpty()) {
            throw new IllegalArgumentException("No specifications found for product with id " + productId);
        }
        specsRepo.deleteAll(specs);
    }
}
