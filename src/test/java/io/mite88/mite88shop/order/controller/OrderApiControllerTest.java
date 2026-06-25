package io.mite88.mite88shop.order.controller;

import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.order.dto.OrderDescription;
import io.mite88.mite88shop.order.dto.OrderItemDescription;
import io.mite88.mite88shop.order.entity.OrderStatus;
import io.mite88.mite88shop.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static io.mite88.mite88shop.global.code.ResponseCode.EMPTY_CART;
import static io.mite88.mite88shop.global.code.ResponseCode.ORDER_CANCEL_NOT_ALLOWED;
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
class OrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private OrderDescription sampleOrder(OrderStatus status) {
        OrderItemDescription item = new OrderItemDescription(1L, 1L, "나이키 운동화", 89000, 2, 178000);
        return new OrderDescription(1L, List.of(item), 178000, status, LocalDateTime.now());
    }

    @Test
    @DisplayName("주문 성공")
    void placeOrder_Success() throws Exception {
        when(orderService.placeOrder("testuser")).thenReturn(sampleOrder(OrderStatus.ORDERED));

        mockMvc.perform(post("/api/orders")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.status").value("ORDERED"))
                .andExpect(jsonPath("$.totalPrice").value(178000));
    }

    @Test
    @DisplayName("주문 실패 - 장바구니 비어있음")
    void placeOrder_EmptyCart() throws Exception {
        when(orderService.placeOrder("testuser")).thenThrow(new BusinessException(EMPTY_CART));

        mockMvc.perform(post("/api/orders")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(EMPTY_CART.getCode()));
    }

    @Test
    @DisplayName("주문 실패 - 비인증")
    void placeOrder_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/orders").with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 주문 목록 조회 성공")
    void findMyOrders_Success() throws Exception {
        when(orderService.findMyOrders("testuser")).thenReturn(List.of(sampleOrder(OrderStatus.ORDERED)));

        mockMvc.perform(get("/api/orders")
                        .with(user("testuser").roles("USER")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1L));
    }

    @Test
    @DisplayName("주문 단건 조회 성공")
    void findById_Success() throws Exception {
        when(orderService.findById("testuser", 1L)).thenReturn(sampleOrder(OrderStatus.ORDERED));

        mockMvc.perform(get("/api/orders/1")
                        .with(user("testuser").roles("USER")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancel_Success() throws Exception {
        when(orderService.cancel("testuser", 1L)).thenReturn(sampleOrder(OrderStatus.CANCELLED));

        mockMvc.perform(patch("/api/orders/1/cancel")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("주문 취소 실패 - 이미 취소된 주문")
    void cancel_NotAllowed() throws Exception {
        when(orderService.cancel("testuser", 1L)).thenThrow(new BusinessException(ORDER_CANCEL_NOT_ALLOWED));

        mockMvc.perform(patch("/api/orders/1/cancel")
                        .with(user("testuser").roles("USER"))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ORDER_CANCEL_NOT_ALLOWED.getCode()));
    }
}
