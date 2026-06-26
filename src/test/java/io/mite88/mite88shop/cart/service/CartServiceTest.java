package io.mite88.mite88shop.cart.service;

import io.mite88.mite88shop.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.cart.dto.CartDescription;
import io.mite88.mite88shop.cart.dto.CartItemRequest;
import io.mite88.mite88shop.cart.entity.Cart;
import io.mite88.mite88shop.cart.entity.CartItem;
import io.mite88.mite88shop.cart.repository.CartItemJpaRepository;
import io.mite88.mite88shop.cart.repository.CartJpaRepository;
import io.mite88.mite88shop.members.dto.Role;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.product.entity.Product;
import io.mite88.mite88shop.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartJpaRepository cartRepository;
    @Mock
    private CartItemJpaRepository cartItemRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private ProductService productService;

    private Member testMember;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testMember = Member.builder().username("testuser").password("pass")
                .email("test@test.com").role(Role.MEMBER).build();
        ReflectionTestUtils.setField(testMember, "id", 1L);

        testProduct = Product.builder().name("나이키 운동화").description("편안한 운동화")
                .price(89000).stock(100).category("신발").build();
        ReflectionTestUtils.setField(testProduct, "id", 1L);
        ReflectionTestUtils.setField(testProduct, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testProduct, "updatedAt", LocalDateTime.now());

        testCart = Cart.createFor(testMember);
        ReflectionTestUtils.setField(testCart, "id", 1L);

        testCartItem = CartItem.of(testCart, testProduct, 2);
        ReflectionTestUtils.setField(testCartItem, "id", 1L);
    }

    @Test
    @DisplayName("장바구니 조회 - 장바구니 없으면 자동 생성")
    void getMyCart_AutoCreate() {
        when(memberService.findByUsername("testuser")).thenReturn(testMember);
        when(cartRepository.findFirstByMember(testMember)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDescription result = cartService.getMyCart("testuser");

        assertThat(result).isNotNull();
        assertThat(result.items()).isEmpty();
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("장바구니 조회 - 기존 장바구니 반환")
    void getMyCart_Existing() {
        when(memberService.findByUsername("testuser")).thenReturn(testMember);
        when(cartRepository.findFirstByMember(testMember)).thenReturn(Optional.of(testCart));

        CartDescription result = cartService.getMyCart("testuser");

        assertThat(result.cartId()).isEqualTo(1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("장바구니 상품 추가 성공 - 새 상품")
    void addItem_NewProduct() {
        CartItemRequest request = new CartItemRequest(1L, 2);
        when(memberService.findByUsername("testuser")).thenReturn(testMember);
        when(cartRepository.findFirstByMember(testMember)).thenReturn(Optional.of(testCart));
        when(productService.getProductOrThrow(1L)).thenReturn(testProduct);
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        cartService.addItem("testuser", request);

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 상품 추가 - 기존 상품 수량 증가")
    void addItem_ExistingProduct_QuantityIncreased() {
        CartItemRequest request = new CartItemRequest(1L, 3);
        when(memberService.findByUsername("testuser")).thenReturn(testMember);
        when(cartRepository.findFirstByMember(testMember)).thenReturn(Optional.of(testCart));
        when(productService.getProductOrThrow(1L)).thenReturn(testProduct);
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct)).thenReturn(Optional.of(testCartItem));

        cartService.addItem("testuser", request);

        assertThat(testCartItem.getQuantity()).isEqualTo(5); // 2 + 3
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    @DisplayName("장바구니 수량 변경 성공")
    void updateItem_Success() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));

        cartService.updateItem("testuser", 1L, 5);

        assertThat(testCartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 수량 변경 실패 - 항목 없음")
    void updateItem_NotFound() {
        when(cartItemRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> cartService.updateItem("testuser", 99L, 5));
        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 성공")
    void removeItem_Success() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);

        cartService.removeItem("testuser", 1L);

        verify(cartItemRepository, times(1)).delete(testCartItem);
    }
}
