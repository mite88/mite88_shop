package io.mite88.mite88shop.product.entity;

import io.mite88.mite88shop.product.dto.ProductUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int price;
    private int stock;
    private String category;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Product(String name, String description, int price, int stock, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 상품 정보 수정
     */
    public void update(ProductUpdateRequest request) {
        this.name = request.name();
        this.description = request.description();
        this.price = request.price();
        this.stock = request.stock();
        this.category = request.category();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재고 차감 - 주문 시 호출 (재고 부족 검증은 서비스 레이어에서 선행)
     */
    public void decreaseStock(int quantity) {
        this.stock -= quantity;
    }
}
