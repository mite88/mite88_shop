package io.mite88.mite88shop.cart.controller;

import io.mite88.mite88shop.cart.dto.CartDescription;
import io.mite88.mite88shop.cart.dto.CartItemDescription;
import io.mite88.mite88shop.cart.dto.CartItemRequest;
import io.mite88.mite88shop.cart.service.CartService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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
class CartApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    private CartDescription sampleCart() {
        CartItemDescription item = new CartItemDescription(1L, 1L, "나이키 운동화", 89000, 2, 178000, 100);
        return new CartDescription(1L, List.of(item), 178000);
    }

    @Test
    @DisplayName("장바구니 조회 성공")
    void getMyCart_Success() throws Exception {
        when(cartService.getMyCart("testuser")).thenReturn(sampleCart());

        mockMvc.perform(get("/api/cart")
                        .with(user("testuser").roles("USER")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.totalPrice").value(178000))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    @DisplayName("장바구니 조회 실패 - 비인증")
    void getMyCart_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("장바구니 상품 추가 성공")
    void addItem_Success() throws Exception {
        CartItemRequest request = new CartItemRequest(1L, 2);
        when(cartService.addItem(eq("testuser"), any(CartItemRequest.class))).thenReturn(sampleCart());

        mockMvc.perform(post("/api/cart/items")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(178000));
    }

    @Test
    @DisplayName("장바구니 수량 변경 성공")
    void updateItem_Success() throws Exception {
        CartDescription updated = new CartDescription(1L,
                List.of(new CartItemDescription(1L, 1L, "나이키 운동화", 89000, 5, 445000, 100)), 445000);
        when(cartService.updateItem("testuser", 1L, 5)).thenReturn(updated);

        mockMvc.perform(patch("/cart/items/1")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .param("quantity", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(445000))
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    @Test
    @DisplayName("장바구니 상품 삭제 성공")
    void removeItem_Success() throws Exception {
        CartDescription emptyCart = new CartDescription(1L, List.of(), 0);
        when(cartService.removeItem("testuser", 1L)).thenReturn(emptyCart);

        mockMvc.perform(delete("/cart/items/1")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));

        verify(cartService, times(1)).removeItem("testuser", 1L);
    }
}
