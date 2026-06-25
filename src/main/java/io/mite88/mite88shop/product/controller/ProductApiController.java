package io.mite88.mite88shop.product.controller;

import io.mite88.mite88shop.product.dto.ProductDescription;
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

    @Override
    @PostMapping
    public ResponseEntity<ProductDescription> save(@Valid @RequestBody ProductSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(request));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ProductDescription> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ProductDescription>> findAll(@RequestParam(required = false) String category) {
        List<ProductDescription> result = (category != null && !category.isBlank())
                ? productService.findByCategory(category)
                : productService.findAll();
        return ResponseEntity.ok(result);
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDescription> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
