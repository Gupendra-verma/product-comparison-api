package com.example.ProductComparison.Service;

import com.example.ProductComparison.DTO.ComparedProductDto;
import com.example.ProductComparison.DTO.ComparisonRowDto;
import com.example.ProductComparison.DTO.ComparisonTableDto;
import com.example.ProductComparison.Entity.Product;
import com.example.ProductComparison.Entity.ProductSpecification;
import com.example.ProductComparison.Mapper.ProductComparisonMapper;
import com.example.ProductComparison.Repository.ProductRepo;
import com.example.ProductComparison.Repository.SpecificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductComparisonService {

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private SpecificationRepo specificationRepo;
    @Autowired
    private ProductComparisonMapper productComparisonMapper;

    @Cacheable(value = "comparison", key = "#productIds")
    public ComparisonTableDto compareProducts(List<Long> productIds) {
        System.out.println("Executing comparison logic...");
        validateComparisonInput(productIds);

        List<Product> products = resolveProductsInRequestedOrder(productIds);
        validateSameCategory(products);

        List<ComparedProductDto> comparedProducts = products.stream()
                .map(ProductComparisonMapper::toComparedProductDto)
                .toList();

        List<Map<String, String>> specsPerProduct = products.stream()
                .map(this::specsMapForProduct)
                .toList();

        LinkedHashSet<String> specNames = orderedSpecNames(specsPerProduct);

        List<ComparisonRowDto> rows = specNames.stream()
                .map(specName -> comparisonRow(specName, specsPerProduct))
                .toList();

        return ComparisonTableDto.builder()
                .products(comparedProducts)
                .rows(rows)
                .build();
    }

    private void validateComparisonInput(List<Long> productIds) {
        if (productIds == null || productIds.size() < 2 || productIds.size() > 4) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Compare requires between 2 and 4 product IDs"
            );
        }
    }

    private List<Product> resolveProductsInRequestedOrder(List<Long> productIds) {
        Map<Long, Product> byId = new HashMap<>();
        productRepo.findAllById(new HashSet<>(productIds))
                .forEach(p -> byId.put(p.getId(), p));

        List<Long> missingIds = productIds.stream()
                .filter(id -> !byId.containsKey(id))
                .distinct()
                .toList();
        if (!missingIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Products not found for IDs: " + missingIds
            );
        }

        return productIds.stream()
                .map(byId::get)
                .toList();
    }

    private void validateSameCategory(List<Product> products) {
        Set<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .collect(HashSet::new, Set::add, Set::addAll);
        if (categoryIds.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Comparison is allowed only for products in the same category"
            );
        }
    }


    private Map<String, String> specsMapForProduct(Product product) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Rating",
                product.getRating() != null
                        ? product.getRating().toString()
                        : "N/A");

        map.put("Total Reviews",
                product.getTotalReviews() != null
                        ? product.getTotalReviews().toString()
                        : "N/A");

        for (ProductSpecification spec : specificationRepo.findByProductId(product.getId())) {
            String name = spec.getSpecType() != null ? spec.getSpecType().getName() : "Unknown";
            map.putIfAbsent(name, spec.getSpecValue());
        }

        return map;
    }

    private LinkedHashSet<String> orderedSpecNames(List<Map<String, String>> specsPerProduct) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        specsPerProduct.forEach(m -> names.addAll(m.keySet()));
        return names;
    }

    private ComparisonRowDto comparisonRow(String specName, List<Map<String, String>> specsPerProduct) {
        List<String> cells = specsPerProduct.stream()
                .map(m -> Optional.ofNullable(m.get(specName)).orElse("N/A"))
                .toList();
        return productComparisonMapper.toComparisonRowDto(specName,cells);

    }
}
