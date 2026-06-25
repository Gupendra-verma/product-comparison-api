package com.example.ProductComparison.Service;

import com.example.ProductComparison.DTO.*;
import com.example.ProductComparison.Entity.Category;
import com.example.ProductComparison.Entity.Product;
import com.example.ProductComparison.Mapper.ProductMapper;
import com.example.ProductComparison.Repository.CategoryRepo;
import com.example.ProductComparison.Repository.ProductRepo;
import com.example.ProductComparison.Repository.ReviewRepo;
import com.example.ProductComparison.Repository.SpecificationRepo;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private SpecificationRepo specificationRepo;
    @Autowired
    private ReviewRepo reviewRepo;
    @Autowired
    private ProductMapper productMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public PagedResponseDto<ProductPageResponseDto> getAllProductsPagedResponse(
            int page,
            int size,
            String sortBy,
            String sortDir,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String brand,
            String categoryName
    ) {
        logger.debug("Product query requested: page={}, size={}, sortBy={}, sortDir={}, " +
                     "filters: minPrice={}, maxPrice={}, brand={}, categoryName={}", 
                     page, size, sortBy, sortDir, minPrice, maxPrice, brand, categoryName);

        // Validate inputs
        if (minPrice != null && minPrice.signum() < 0) {
            logger.warn("Invalid minPrice provided: {}", minPrice);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice must be 0 or more");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            logger.warn("Invalid maxPrice provided: {}", maxPrice);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "maxPrice must be 0 or more");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            logger.warn("Invalid price range: minPrice={} > maxPrice={}", minPrice, maxPrice);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice must be <= maxPrice");
        }

        validateSortBy(sortBy);

        // If no filters applied, use cached method for better performance
        boolean hasFilters = minPrice != null || maxPrice != null || 
                           (brand != null && !brand.isBlank()) || 
                           (categoryName != null && !categoryName.isBlank());

        if (!hasFilters) {
            logger.debug("No filters applied - using cached query");
            return getUnfilteredProductsPagedResponse(page, size, sortBy, sortDir);
        }

        logger.debug("Filters applied - bypassing cache for specificity");
        return getFilteredProductsPagedResponse(page, size, sortBy, sortDir, minPrice, maxPrice, brand, categoryName);
    }

    /**
     * Cached method for unfiltered product listing
     * Cache key includes only pagination and sorting to maximize hit rate
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productListUnfiltered", key = "'page_' + #page + '_size_' + #size + '_sort_' + #sortBy + '_' + #sortDir")
    private PagedResponseDto<ProductPageResponseDto> getUnfilteredProductsPagedResponse(
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        logger.debug("Fetching unfiltered products from database: page={}, size={}, sortBy={}, sortDir={}", 
                     page, size, sortBy, sortDir);

        Sort.Direction direction = parseSortDirection(sortDir);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Product> spec = (root, query, cb) -> cb.conjunction();
        Page<ProductResponseDto> result = productRepo.findAll(spec, pageable)
                .map(productMapper::toProductResponseDto);
        logger.info("Unfiltered product query completed: found {} products on page {}", 
                   result.getNumberOfElements(), page);

        String message = result.isEmpty()
                ? "No product"
                : "Total products: " + result.getTotalElements();

        return PagedResponseDto.<ProductPageResponseDto>builder()
                .message(message)
                .data(productMapper.toProductPageResponseDto(result))
                .build();
    }

    /**
     * Non-cached method for filtered product listing
     * Filters are too specific for effective caching, so this bypasses the cache
     */
    @Transactional(readOnly = true)
    private PagedResponseDto<ProductPageResponseDto> getFilteredProductsPagedResponse(
            int page,
            int size,
            String sortBy,
            String sortDir,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String brand,
            String categoryName
    ) {
        logger.debug("Fetching filtered products from database: minPrice={}, maxPrice={}, brand={}, categoryName={}", 
                     minPrice, maxPrice, brand, categoryName);

        Sort.Direction direction = parseSortDirection(sortDir);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        if (brand != null && !brand.isBlank()) {
            String b = brand.trim().toLowerCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("brand")), b));
        }
        if (categoryName != null && !categoryName.isBlank()) {
            String cat = categoryName.trim().toLowerCase();
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.join("category").get("name")), cat)
            );
        }

        Page<ProductResponseDto> result = productRepo.findAll(spec, pageable)
                .map(productMapper::toProductResponseDto);
        logger.info("Filtered product query completed: found {} total products matching criteria", result.getTotalElements());

        String message = result.isEmpty()
                ? "No product"
                : "Total products: " + result.getTotalElements();

        return PagedResponseDto.<ProductPageResponseDto>builder()
                .message(message)
                .data(productMapper.toProductPageResponseDto(result))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProductsByName(String keyword, int page, int size, String sortBy, String sortDir) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyword must not be blank");
        }
        validateSortBy(sortBy);
        Sort.Direction direction = parseSortDirection(sortDir);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return productRepo.findByNameContainingIgnoreCase(keyword, pageable)
                .map(productMapper::toProductResponseDto);
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<ProductPageResponseDto> searchProductsByNamePagedResponse(
            String keyword,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Page<ProductResponseDto> result = searchProductsByName(keyword, page, size, sortBy, sortDir);
        String message = result.isEmpty() ? "No products found" : "Products found: " + result.getTotalElements();
        return PagedResponseDto.<ProductPageResponseDto>builder()
                .message(message)
                .data(productMapper.toProductPageResponseDto(result))
                .build();
    }

    @Cacheable(value = "products", key = "#productId")
    public ProductResponseDto getProductById(Long productId) {
        return productRepo.findById(productId)
                .map(productMapper::toProductResponseDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product with id " + productId + " does not exist"));
    }

    @Caching(evict = {
            @CacheEvict(value = "productListUnfiltered", allEntries = true),
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "comparison", allEntries = true)})
    public void addProduct(ProductRequestDto productRequestDto) {
        if (productRequestDto==null) {
            throw new IllegalArgumentException("Product request cannot be null");
        }

        if (productRepo.existsByNameIgnoreCaseAndBrandIgnoreCaseAndDeletedFalse(
                productRequestDto.getName().trim(),
                productRequestDto.getBrand().trim())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product with same name and brand already exists"
            );
        }
       Category category = categoryRepo.findByName(productRequestDto.getCategoryName());
        if (category == null) {
            category = new Category();
            category.setName(productRequestDto.getCategoryName());
            categoryRepo.save(category);
        }

        Product product = new Product();
        product.setName(productRequestDto.getName());
        product.setBrand(productRequestDto.getBrand());
        product.setPrice(productRequestDto.getPrice());
        product.setDescription(productRequestDto.getDescription());
        product.setCategoryId(category.getId());
        productRepo.save(product);
    }

    @Caching(evict = {
            @CacheEvict(value = "productList", allEntries = true),
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "specifications", key = "#productId"),
            @CacheEvict(value = "comparison", allEntries = true)})
    public void softDeleteProduct(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product with id " + productId + " does not exist"));

        if(product.getDeleted()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product with id " + productId + " is already deleted");
        }

        product.setDeleted(true);
        productRepo.save(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productList", allEntries = true),
            @CacheEvict(value = "products", key = "#productId"),
            @CacheEvict(value = "specifications", key = "#productId"),
            @CacheEvict(value = "comparison", allEntries = true)})
    public void hardDeleteProduct(Long productId) {
        Product product = productRepo.findByIdIncludingDeleted(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product with id " + productId + " does not exist"));

        if(!product.getDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product with id " + productId + " must be soft deleted, before hard delete");
        }
        if(!reviewRepo.findByProductId(productId).isEmpty()) {
            reviewRepo.deleteAll(reviewRepo.findByProductId(productId));
        }
        if (!specificationRepo.findByProductId(productId).isEmpty()) {
            specificationRepo.deleteAll(specificationRepo.findByProductId(productId));
        }

        entityManager.createNativeQuery("DELETE FROM products WHERE id = :id")
                .setParameter("id", productId)
                .executeUpdate();
    }

    private static Sort.Direction parseSortDirection(String sortDir) {
        if ("desc".equalsIgnoreCase(sortDir)) {
            return Sort.Direction.DESC;
        }
        if ("asc".equalsIgnoreCase(sortDir)) {
            return Sort.Direction.ASC;
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "sortDir must be either 'asc' or 'desc'"
        );
    }

    private static void validateSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sortBy must not be blank");
        }
        // whitelist: prevents runtime errors & avoids exposing internal fields
        if (!Set.of("id", "name", "brand", "price", "rating", "totalReviews", "stockQuantity", "createdAt")
                .contains(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sortBy must be one of: id, name, brand, price, rating, totalReviews, stockQuantity, createdAt"
            );
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "productList", allEntries = true),
            @CacheEvict(value = "comparison", allEntries = true)})
    public BulkProductResponseDto bulkAddProduct(@Valid List<ProductRequestDto> productDtos) {

            if (productDtos == null || productDtos.isEmpty()) {
                throw new IllegalArgumentException("Product list cannot be null or empty");
            }

            int inserted = 0;
            List<String> skippedProducts = new ArrayList<>();

            for (ProductRequestDto dto : productDtos) {

                String productName = dto.getName().trim();
                String brandName = dto.getBrand().trim();

                boolean exists =
                        productRepo.existsByNameIgnoreCaseAndBrandIgnoreCaseAndDeletedFalse(
                                productName,
                                brandName
                        );

                if (exists) {
                    skippedProducts.add(productName + " - " + brandName);
                    continue;
                }

                Category category =
                        categoryRepo.findByName(dto.getCategoryName());

                if (category == null) {
                    category = new Category();
                    category.setName(dto.getCategoryName().trim());

                    category = categoryRepo.save(category);
                }

                Product product = productMapper.toEntity(dto);

                product.setCategoryId(category.getId());

                productRepo.save(product);

                inserted++;
            }

            return new BulkProductResponseDto(
                    productDtos.size(),
                    inserted,
                    skippedProducts.size(),
                    skippedProducts
            );

    }
}
