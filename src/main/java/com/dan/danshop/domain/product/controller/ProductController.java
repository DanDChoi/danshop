package com.dan.danshop.domain.product.controller;

import com.dan.danshop.domain.product.dto.AddRequest;
import com.dan.danshop.domain.product.dto.ProductResponse;
import com.dan.danshop.domain.product.dto.UpdateRequest;
import com.dan.danshop.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Product", description = "상품 관련 API")
public class ProductController {

    private final ProductService productService;

    @PostMapping(value = "/product")
    @Operation(summary = "상품 등록")
    public ResponseEntity<?> addProduct(@RequestBody AddRequest addRequest) {
        productService.addProduct(addRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("상품 등록 완료");
    }

    @PatchMapping(value = "/product/{productNo}")
    @Operation(summary = "상품 수정")
    public ResponseEntity<?> updateProduct(@PathVariable Long productNo, @RequestBody UpdateRequest updateRequest) {

        productService.updateProduct(productNo, updateRequest);

        return ResponseEntity.status(HttpStatus.OK).body("상품 수정 완료");
    }


    @GetMapping(value = "/product/{productNo}")
    @Operation(summary = "상품 조회")
    public ResponseEntity<ProductResponse> findProduct(@PathVariable Long productNo) {
        return ResponseEntity.ok(productService.findByProductNo(productNo));
    }

    @GetMapping("/product")
    @Operation(summary = "상품 목록 조회")
    public ResponseEntity<Page<ProductResponse>> findProductList(@RequestParam(required = false, defaultValue = "0") int page,
                                                                 @RequestParam(required = false, defaultValue = "10") int size,
                                                                 @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productService.findProductList(page, size, keyword));
    }


    @DeleteMapping(value = "/product/{productNo}")
    @Operation(summary = "상품 삭제")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productNo) {
        productService.deleteProduct(productNo);
        return ResponseEntity.status(HttpStatus.OK).body("상품 삭제 완료");
    }


}
