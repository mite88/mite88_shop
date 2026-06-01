package io.mite88.mite88shop.product.controller;

import io.mite88.mite88shop.product.dto.ProductDescription;
import io.mite88.mite88shop.product.dto.ProductSaveRequest;
import io.mite88.mite88shop.product.dto.ProductUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ProductApiDocs {

    @Operation(summary = "상품 등록", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ProductDescription.class)))
    ResponseEntity<ProductDescription> save(@RequestBody ProductSaveRequest request);

    @Operation(summary = "상품 단건 조회")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProductDescription.class)))
    @ApiResponse(responseCode = "404", content = @Content)
    ResponseEntity<ProductDescription> findById(@PathVariable Long id);

    @Operation(summary = "상품 전체/카테고리별 조회")
    @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductDescription.class))))
    ResponseEntity<List<ProductDescription>> findAll(@RequestParam(required = false) String category);

    @Operation(summary = "상품 수정", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ProductDescription.class)))
    ResponseEntity<ProductDescription> update(@PathVariable Long id, @RequestBody ProductUpdateRequest request);

    @Operation(summary = "상품 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204", content = @Content)
    ResponseEntity<Void> delete(@PathVariable Long id);
}
