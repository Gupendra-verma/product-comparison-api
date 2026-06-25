package com.example.ProductComparison.Service;

import com.example.ProductComparison.DTO.ProductResponseDto;
import com.example.ProductComparison.DTO.ProductPageResponseDto;
import com.example.ProductComparison.DTO.PagedResponseDto;
import com.example.ProductComparison.Entity.Category;
import com.example.ProductComparison.Entity.Product;
import com.example.ProductComparison.Mapper.ProductMapper;
import com.example.ProductComparison.Repository.CategoryRepo;
import com.example.ProductComparison.Repository.ProductRepo;
import com.example.ProductComparison.Repository.ReviewRepo;
import com.example.ProductComparison.Repository.SpecificationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.cache.type=none"})
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockitoBean
    private ProductRepo productRepo;

    @MockitoBean
    private CategoryRepo categoryRepo;

    @MockitoBean
    private SpecificationRepo specificationRepo;
    @MockitoBean
    private ProductResponseDto testProductResponseDto;
    @MockitoBean
    private ProductPageResponseDto productPageResponseDto;
    @MockitoBean
    private ReviewRepo reviewRepo;
    @MockitoBean
    private Product testProduct;
    @MockitoBean
    private Category testCategory;
    @MockitoBean
    private ProductMapper productMapper;


    @BeforeEach

    void setUp() {

        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .brand("TestBrand")
                .price(BigDecimal.valueOf(99.99))
                .category(testCategory)
                .build();

        testProductResponseDto = ProductResponseDto.builder()
                .id(1L)
                .name("Test Product")
                .brand("TestBrand")
                .price(BigDecimal.valueOf(99.99))
                .build();

        productPageResponseDto = ProductPageResponseDto.builder()
                .products(List.of(testProductResponseDto))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .last(true)
                .build();

        when(productMapper.toProductResponseDto(any(Product.class)))
                .thenReturn(testProductResponseDto);

        when(productMapper.toProductPageResponseDto(any(Page.class)))
                .thenReturn(productPageResponseDto);
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById_WithValidId_ReturnsProduct() {
        when(productRepo.findById(1L))
                .thenReturn(Optional.of(testProduct));

        when(productMapper.toProductResponseDto(testProduct))
                .thenReturn(testProductResponseDto);

        ProductResponseDto result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());

        verify(productRepo).findById(1L);
        verify(productMapper).toProductResponseDto(testProduct);
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testGetProductById_WithInvalidId_ThrowsException() {
        when(productRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> productService.getProductById(999L));

//        verify(productRepo, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should search products by keyword successfully")
    void testSearchProductsByName_WithValidKeyword_ReturnsResults() {
        Page<Product> productPage = new PageImpl<>(
                List.of(testProduct),
                PageRequest.of(0, 10),
                1L
        );

        when(productRepo.findByNameContainingIgnoreCase(anyString(), any()))
                .thenReturn(productPage);

        PagedResponseDto<ProductPageResponseDto> result = productService
                .searchProductsByNamePagedResponse("Test", 0, 10, "id", "asc");

        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals("Products found: 1", result.getMessage());
        verify(productRepo, times(1)).findByNameContainingIgnoreCase(anyString(), any());
    }

    @Test
    @DisplayName("Should return empty result when no products match search")
    void testSearchProductsByName_WithNoMatches_ReturnsEmpty() {
        Page<Product> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0L
        );

        when(productRepo.findByNameContainingIgnoreCase(anyString(), any()))
                .thenReturn(emptyPage);

        PagedResponseDto<ProductPageResponseDto> result = productService
                .searchProductsByNamePagedResponse("NonExistent", 0, 10, "id", "asc");

        assertNotNull(result);
        assertEquals("No products found", result.getMessage());
        verify(productRepo, times(1)).findByNameContainingIgnoreCase(anyString(), any());
    }

    @Test
    @DisplayName("Should throw exception for blank search keyword")
    void testSearchProductsByName_WithBlankKeyword_ThrowsException() {
        assertThrows(ResponseStatusException.class,
                () -> productService.searchProductsByName("", 0, 10, "id", "asc"));
    }

    @Test
    @DisplayName("Should validate minimum price parameter")
    void testGetAllProducts_WithNegativeMinPrice_ThrowsException() {
        assertThrows(ResponseStatusException.class,
                () -> productService.getAllProductsPagedResponse(
                        0, 10, "id", "asc",
                        BigDecimal.valueOf(-10), // negative price
                        null, null, null
                ));
    }

    @Test
    @DisplayName("Should validate that minPrice <= maxPrice")
    void testGetAllProducts_WithMinPriceGreaterThanMaxPrice_ThrowsException() {
        assertThrows(ResponseStatusException.class,
                () -> productService.getAllProductsPagedResponse(
                        0, 10, "id", "asc",
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(50), // minPrice > maxPrice
                        null, null
                ));
    }

    @Test
    @DisplayName("Should allow valid price range filtering")
    void testGetAllProducts_WithValidPriceRange_ReturnsFilteredResults() {
        Page<Product> filteredPage = new PageImpl<>(
                List.of(testProduct),
                PageRequest.of(0, 10),
                1L
        );

        when(productRepo.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(filteredPage);

        PagedResponseDto<ProductPageResponseDto> result = productService
                .getAllProductsPagedResponse(
                        0, 10, "id", "asc",
                        BigDecimal.valueOf(50),
                        BigDecimal.valueOf(150),
                        null, null
                );

        assertNotNull(result);
        assertEquals("Total products: 1", result.getMessage());
    }

    @Test
    @DisplayName("Should handle pagination with correct defaults")
    void testGetAllProducts_WithDefaultPagination_Success() {
        Page<Product> productPage = new PageImpl<>(
                List.of(testProduct),
                PageRequest.of(0, 10),
                1L
        );

        when(productRepo.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(productPage);

        PagedResponseDto<ProductPageResponseDto> result = productService
                .getAllProductsPagedResponse(0, 10, "id", "asc", null, null, null, null);

        assertNotNull(result);
        verify(productRepo, times(1)).findAll(any(Specification.class),
                any(Pageable.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid sortBy parameter")
    void testGetAllProducts_WithInvalidSortBy_ThrowsException() {
        assertThrows(ResponseStatusException.class,
                () -> productService.getAllProductsPagedResponse(
                        0, 10, "invalidColumn", "asc",
                        null, null, null, null
                ));
    }
}
