package io.mite88.mite88shop.product.controller;

import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.product.dto.ProductDescription;
import io.mite88.mite88shop.product.dto.ProductSaveRequest;
import io.mite88.mite88shop.product.dto.ProductUpdateRequest;
import io.mite88.mite88shop.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static io.mite88.mite88shop.global.code.ResponseCode.PRODUCT_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-config.properties")
class ProductApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private ProductDescription sampleProduct() {
        return new ProductDescription(1L, "나이키 운동화", "편안한 운동화", 89000, 100, "신발", LocalDateTime.now());
    }

    @Test
    @DisplayName("상품 등록 성공")
    void save_Success() throws Exception {
        ProductSaveRequest request = new ProductSaveRequest("나이키 운동화", "편안한 운동화", 89000, 100, "신발");
        when(productService.save(any(ProductSaveRequest.class))).thenReturn(sampleProduct());

        mockMvc.perform(post("/api/products")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("나이키 운동화"))
                .andExpect(jsonPath("$.price").value(89000));
    }

    @Test
    @DisplayName("상품 등록 실패 - 비인증")
    void save_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductSaveRequest("나이키", "설명", 89000, 100, "신발"))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        verify(productService, never()).save(any());
    }

    @Test
    @DisplayName("상품 단건 조회 성공")
    void findById_Success() throws Exception {
        when(productService.findById(1L)).thenReturn(sampleProduct());

        mockMvc.perform(get("/api/products/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("나이키 운동화"));
    }

    @Test
    @DisplayName("상품 단건 조회 실패 - 상품 없음")
    void findById_NotFound() throws Exception {
        when(productService.findById(99L)).thenThrow(new BusinessException(PRODUCT_NOT_FOUND));

        mockMvc.perform(get("/api/products/99"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(PRODUCT_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("상품 전체 조회 성공")
    void findAll_Success() throws Exception {
        when(productService.findAll()).thenReturn(List.of(sampleProduct()));

        mockMvc.perform(get("/api/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("나이키 운동화"));
    }

    @Test
    @DisplayName("카테고리별 상품 조회 성공")
    void findByCategory_Success() throws Exception {
        when(productService.findByCategory("신발")).thenReturn(List.of(sampleProduct()));

        mockMvc.perform(get("/api/products").param("category", "신발"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("신발"));
    }

    @Test
    @DisplayName("상품 수정 성공")
    void update_Success() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest("나이키 운동화 v2", "업그레이드", 95000, 50, "신발");
        ProductDescription updated = new ProductDescription(1L, "나이키 운동화 v2", "업그레이드", 95000, 50, "신발", LocalDateTime.now());
        when(productService.update(anyLong(), any(ProductUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/products/1")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("나이키 운동화 v2"))
                .andExpect(jsonPath("$.price").value(95000));
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void delete_Success() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(productService, times(1)).delete(1L);
    }
}
