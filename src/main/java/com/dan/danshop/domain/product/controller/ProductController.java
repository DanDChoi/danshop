package com.dan.danshop.domain.product.controller;

import com.dan.danshop.domain.product.dto.AddRequest;
import com.dan.danshop.domain.product.dto.ProductCursorResponse;
import com.dan.danshop.domain.product.dto.ProductResponse;
import com.dan.danshop.domain.product.dto.ProductSearchCondition;
import com.dan.danshop.domain.product.dto.UpdateRequest;
import com.dan.danshop.domain.product.service.ProductService;
import com.dan.danshop.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@Tag(name = "Product", description = "상품 관련 API")
public class ProductController {

    private final ProductService productService;

    @PostMapping(value = "/product")
    @Operation(summary = "상품 등록")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "201", description = "상품 등록 성공")
    @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> addProduct(@Valid @RequestBody AddRequest addRequest) {
        productService.addProduct(addRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("상품 등록 완료");
    }

    @PatchMapping(value = "/product/{productNo}")
    @Operation(summary = "상품 수정")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "상품 수정 성공")
    @ApiResponse(responseCode = "404", description = "상품 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> updateProduct(@PathVariable Long productNo, @Valid @RequestBody UpdateRequest updateRequest) {

        productService.updateProduct(productNo, updateRequest);

        return ResponseEntity.status(HttpStatus.OK).body("상품 수정 완료");
    }


    @GetMapping(value = "/product/{productNo}")
    @Operation(summary = "상품 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "상품 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ProductResponse> findProduct(@PathVariable Long productNo) {
        return ResponseEntity.ok(productService.findByProductNo(productNo));
    }

    @GetMapping("/product")
    @Operation(summary = "상품 목록 조회 (offset 페이지네이션, 동적 정렬)")
    public ResponseEntity<Page<ProductResponse>> findProductList(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "latest") String sort) {
        ProductSearchCondition condition = new ProductSearchCondition(keyword, category, minPrice, maxPrice, sort);
        return ResponseEntity.ok(productService.findProductList(page, size, condition));
    }

    @GetMapping("/product/scroll")
    @Operation(summary = "상품 목록 조회 (no-offset 커서 페이지네이션)")
    public ResponseEntity<ProductCursorResponse> findProductListNoOffset(
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        ProductSearchCondition condition = new ProductSearchCondition(keyword, category, minPrice, maxPrice, null);
        return ResponseEntity.ok(productService.findProductListNoOffset(condition, lastId, size));
    }


    @DeleteMapping(value = "/product/{productNo}")
    @Operation(summary = "상품 삭제")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "상품 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "관리자 권한 필요",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> deleteProduct(@PathVariable Long productNo) {
        productService.deleteProduct(productNo);
        return ResponseEntity.status(HttpStatus.OK).body("상품 삭제 완료");
    }


}
