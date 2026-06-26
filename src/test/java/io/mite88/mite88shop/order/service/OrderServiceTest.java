package io.mite88.mite88shop.order.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.cart.entity.Cart;
import io.mite88.mite88shop.cart.entity.CartItem;
import io.mite88.mite88shop.cart.repository.CartJpaRepository;
import io.mite88.mite88shop.members.dto.Role;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.order.dto.OrderDescription;
import io.mite88.mite88shop.order.entity.Order;
import io.mite88.mite88shop.order.entity.OrderStatus;
import io.mite88.mite88shop.order.repository.OrderJpaRepository;
import io.mite88.mite88shop.product.entity.Product;
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
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderJpaRepository orderRepository;
    @Mock
    private CartJpaRepository cartRepository;
    @Mock
    private MemberService memberService;

    private Member testMember;
    private Product testProduct;
    private Cart testCart;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testMember = Member.builder().username("testuser").password("pass")
                .email("test@test.com").role(Role.MEMBER).build();
        ReflectionTestUtils.setField(testMember, "id", 1L);

        testProduct = Product.builder().name("나이키 운동화").description("편안한 운동화")
                .price(89000).stock(10).category("신발").build();
        ReflectionTestUtils.setField(testProduct, "id", 1L);
        ReflectionTestUtils.setField(testProduct, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testProduct, "updatedAt", LocalDateTime.now());

        testCart = Cart.createFor(testMember);
        ReflectionTestUtils.setField(testCart, "id", 1L);

        CartItem cartItem = CartItem.of(testCart, testProduct, 2);
        ReflectionTestUtils.setField(cartItem, "id", 1L);
        testCart.getCartItems().add(cartItem);

        testOrder = Order.createFor(testMember);
        ReflectionTestUtils.setField(testOrder, "id", 1L);
    }

    @Test
    @DisplayName("주문 성공")
    void placeOrder_Success() {
        when(memberService.findByUsername("testuser")).thenReturn(testMember);
        when(cartRepository.findFirstByMember(testMember)).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderDescription result = orderService.placeOrder("testuser");

        assertThat(result).isNotNull();
        assertThat(testProduct.getStock()).isEqualTo(8); // 10 - 2
        assertThat(testCart.getCartItems()).isEmpty();
    }

    @Test
    @DisplayName("주문 실패 - 장바구니 없음")
    void placeOrder_CartNotFound() {
        when(memberService.findByUsername("testuser")).thenReturn(testMember);
        when(cartRepository.findFirstByMember(testMember)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.placeOrder("testuser"));
        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.EMPTY_CART);
    }

    @Test
    @DisplayName("주문 실패 - 재고 부족")
    void placeOrder_OutOfStock() {
        ReflectionTestUtils.setField(testProduct, "stock", 1); // 재고 1인데 2개 주문
        when(memberService.findByUsername("testuser")).thenReturn(testMember);
        when(cartRepository.findFirstByMember(testMember)).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.placeOrder("testuser"));
        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("내 주문 목록 조회 성공")
    void findMyOrders_Success() {
        when(memberService.findByUsername("testuser")).thenReturn(testMember);
        when(orderRepository.findByMemberOrderByCreatedAtDesc(testMember)).thenReturn(List.of(testOrder));

        List<OrderDescription> result = orderService.findMyOrders("testuser");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("주문 단건 조회 성공")
    void findById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderDescription result = orderService.findById("testuser", 1L);

        assertThat(result.orderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("주문 단건 조회 실패 - 본인 주문 아님")
    void findById_NotOwner() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.findById("otheruser", 1L));
        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancel_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        OrderDescription result = orderService.cancel("testuser", 1L);

        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("주문 취소 실패 - 이미 취소된 주문")
    void cancel_AlreadyCancelled() {
        testOrder.cancel();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancel("testuser", 1L));
        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.ORDER_CANCEL_NOT_ALLOWED);
    }
}
