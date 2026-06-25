package io.mite88.mite88shop.product.repository;

import io.mite88.mite88shop.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
}
