package com.dan.danshop.domain.product.repository;

import com.dan.danshop.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> searchProducts(String keyword, Pageable pageable);
}
