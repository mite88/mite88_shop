package io.mite88.mite88shop.product.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.product.dto.ProductDescription;
import io.mite88.mite88shop.product.dto.ProductSaveRequest;
import io.mite88.mite88shop.product.dto.ProductUpdateRequest;
import io.mite88.mite88shop.product.entity.Product;
import io.mite88.mite88shop.product.mapper.ProductMapper;
import io.mite88.mite88shop.product.repository.ProductJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductJpaRepository productRepository;

    /**
     * 상품 등록
     */
    @Transactional
    public ProductDescription save(ProductSaveRequest request) {
        Product product = Product.builder()
                .name(request.name()).description(request.description())
                .price(request.price()).stock(request.stock()).category(request.category())
                .build();
        return ProductMapper.toDescription(productRepository.save(product));
    }

    /**
     * 상품 단건 조회
     */
    public ProductDescription findById(Long id) {
        return ProductMapper.toDescription(getProductOrThrow(id));
    }

    /**
     * 전체 상품 목록 조회
     */
    public List<ProductDescription> findAll() {
        return productRepository.findAll().stream().map(ProductMapper::toDescription).toList();
    }

    /**
     * 카테고리별 상품 목록 조회
     */
    public List<ProductDescription> findByCategory(String category) {
        return productRepository.findByCategory(category).stream().map(ProductMapper::toDescription).toList();
    }

    /**
     * 상품 정보 수정
     */
    @Transactional
    public ProductDescription update(Long id, ProductUpdateRequest request) {
        Product product = getProductOrThrow(id);
        product.update(request);
        return ProductMapper.toDescription(product);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * 상품 조회 - 없으면 BusinessException 던짐
     */
    public Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.PRODUCT_NOT_FOUND));
    }
}
