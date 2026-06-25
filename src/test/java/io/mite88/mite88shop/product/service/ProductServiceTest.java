package io.mite88.mite88shop.product.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.product.dto.ProductDescription;
import io.mite88.mite88shop.product.dto.ProductSaveRequest;
import io.mite88.mite88shop.product.dto.ProductUpdateRequest;
import io.mite88.mite88shop.product.entity.Product;
import io.mite88.mite88shop.product.repository.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductJpaRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .name("나이키 운동화").description("편안한 운동화")
                .price(89000).stock(100).category("신발")
                .build();
        ReflectionTestUtils.setField(testProduct, "id", 1L);
        ReflectionTestUtils.setField(testProduct, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testProduct, "updatedAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("상품 등록 성공")
    void save_Success() {
        ProductSaveRequest request = new ProductSaveRequest("나이키 운동화", "편안한 운동화", 89000, 100, "신발");
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductDescription result = productService.save(request);

        assertThat(result.name()).isEqualTo("나이키 운동화");
        assertThat(result.price()).isEqualTo(89000);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 단건 조회 성공")
    void findById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductDescription result = productService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("나이키 운동화");
    }

    @Test
    @DisplayName("상품 단건 조회 실패 - 상품 없음")
    void findById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> productService.findById(99L));
        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("상품 전체 조회 성공")
    void findAll_Success() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        List<ProductDescription> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("나이키 운동화");
    }

    @Test
    @DisplayName("카테고리별 상품 조회 성공")
    void findByCategory_Success() {
        when(productRepository.findByCategory("신발")).thenReturn(List.of(testProduct));

        List<ProductDescription> result = productService.findByCategory("신발");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("신발");
    }

    @Test
    @DisplayName("상품 수정 성공")
    void update_Success() {
        ProductUpdateRequest request = new ProductUpdateRequest("나이키 운동화 v2", "업그레이드", 95000, 50, "신발");
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductDescription result = productService.update(1L, request);

        assertThat(result.name()).isEqualTo("나이키 운동화 v2");
        assertThat(result.price()).isEqualTo(95000);
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void delete_Success() {
        doNothing().when(productRepository).deleteById(1L);

        productService.delete(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }
}
