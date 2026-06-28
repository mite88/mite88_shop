package io.mite88.mite88shop.product.controller;

import io.mite88.mite88shop.product.dto.ProductDescription;
import io.mite88.mite88shop.product.dto.ProductPageResponse;
import io.mite88.mite88shop.product.dto.ProductSaveRequest;
import io.mite88.mite88shop.product.dto.ProductUpdateRequest;
import io.mite88.mite88shop.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController implements ProductApiDocs {

    private final ProductService productService;

    /**
     * 상품 등록 (ADMIN 전용)
     */
    @Override
    @PostMapping
    public ResponseEntity<ProductDescription> save(@Valid @RequestBody ProductSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(request));
    }

    /**
     * 상품 단건 조회
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ProductDescription> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    /**
     * 상품 목록 조회 - category 파라미터 있으면 카테고리별 필터링
     */
    @Override
    @GetMapping
    public ResponseEntity<List<ProductDescription>> findAll(@RequestParam(required = false) String category) {
        List<ProductDescription> result = (category != null && !category.isBlank())
                ? productService.findByCategory(category)
                : productService.findAll();
        return ResponseEntity.ok(result);
    }

    /**
     * 상품 목록 페이징 조회
     */
    @GetMapping("/paged")
    public ResponseEntity<ProductPageResponse> findAllPaged(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        ProductPageResponse result = (category != null && !category.isBlank())
                ? productService.findByCategoryPaged(category, page, size)
                : productService.findAllPaged(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 상품 수정 (ADMIN 전용)
     */
    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDescription> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    /**
     * 상품 삭제 (ADMIN 전용)
     */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
