package com.dan.danshop.domain.product.service;

import com.dan.danshop.domain.product.dto.AddRequest;
import com.dan.danshop.domain.product.dto.ProductResponse;
import com.dan.danshop.domain.product.dto.UpdateRequest;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static com.dan.danshop.global.exception.ErrorCode.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @CacheEvict(value = "products", allEntries = true)
    public void addProduct(AddRequest addRequest) {

        Product addProduct = Product.from(addRequest);

        productRepository.save(addProduct);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void updateProduct(Long productNo, UpdateRequest updateRequest) {
        Product product = productRepository.findById(productNo)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        product.update(updateRequest);
        productRepository.save(product);

    }

    public ProductResponse findByProductNo(Long productNo) {
        Product product = productRepository.findById(productNo)
                .orElseThrow(() -> new BusinessException((PRODUCT_NOT_FOUND)));
        return ProductResponse.from(product);
    }

    @Cacheable("products")
    public Page<ProductResponse> findProductList(int page, int size, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return productRepository.searchProducts(keyword, pageRequest)
                .map(ProductResponse::from);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long productNo) {
        productRepository.deleteById(productNo);
    }
}
