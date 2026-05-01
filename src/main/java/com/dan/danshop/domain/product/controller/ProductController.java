package com.dan.danshop.domain.product.controller;

import com.dan.danshop.domain.product.dto.AddRequest;
import com.dan.danshop.domain.product.dto.ProductResponse;
import com.dan.danshop.domain.product.dto.UpdateRequest;
import com.dan.danshop.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping(value = "/product")
    public ResponseEntity<?> addProduct(@RequestBody AddRequest addRequest) {
        productService.addProduct(addRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("상품 등록 완료");
    }

    @PatchMapping(value = "/product/{productNo}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productNo, @RequestBody UpdateRequest updateRequest) {

        productService.updateProduct(productNo, updateRequest);

        return ResponseEntity.status(HttpStatus.OK).body("상품 수정 완료");
    }


    @GetMapping(value = "/product/{productNo}")
    public ResponseEntity<ProductResponse> findProduct(@PathVariable Long productNo) {
        return ResponseEntity.ok(productService.findByProductNo(productNo));
    }

    @GetMapping("/product")
    public ResponseEntity<Page<ProductResponse>> findProductList(@RequestParam(required = false, defaultValue = "0") int page,
                                                                 @RequestParam(required = false, defaultValue = "10") int size,
                                                                 @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productService.findProductList(page, size, keyword));
    }


    @DeleteMapping(value = "/product/{productNo}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productNo) {
        productService.deleteProduct(productNo);
        return ResponseEntity.status(HttpStatus.OK).body("상품 삭제 완료");
    }


}
