package com.dan.danshop.domain.product.service;

import com.dan.danshop.domain.product.dto.AddRequest;
import com.dan.danshop.domain.product.dto.ProductCursorResponse;
import com.dan.danshop.domain.product.dto.ProductResponse;
import com.dan.danshop.domain.product.dto.ProductSearchCondition;
import com.dan.danshop.domain.product.dto.UpdateRequest;
import com.dan.danshop.domain.product.entity.Product;
import com.dan.danshop.domain.product.repository.ProductRepository;
import com.dan.danshop.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dan.danshop.global.exception.ErrorCode.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // 캐시 프록시를 거쳐 findProductPageCache를 호출하기 위한 self 참조.
    // 같은 빈 안에서 이 메서드를 직접 호출하면 @Cacheable AOP 프록시를 우회해
    // 캐싱이 적용되지 않는다. @Lazy로 지연 프록시를 주입해 순환 참조를 피한다.
    @Autowired
    @Lazy
    private ProductService self;

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

    public Page<ProductResponse> findProductList(int page, int size, ProductSearchCondition condition) {
        ProductPageCache cache = self.findProductPageCache(page, size, condition);
        PageRequest pageRequest = PageRequest.of(page, size);
        return new PageImpl<>(cache.content(), pageRequest, cache.totalElements());
    }

    /**
     * 캐시에는 content + totalElements만 저장한다. Page(PageImpl)를 그대로
     * 캐싱하면 Redis에서 다시 읽을 때 Jackson이 PageImpl을 역직렬화하지 못해
     * 캐시 히트 시 500이 났다(spring-data-commons의 PageModule은 HTTP 응답
     * 직렬화 전용이라 이 왕복을 지원하지 않는다). List+long은 순수 POJO라
     * 문제없이 캐싱된다.
     */
    @Cacheable(value = "products", key = "#page + '_' + #size + '_' + #condition.toString()")
    public ProductPageCache findProductPageCache(int page, int size, ProductSearchCondition condition) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ProductResponse> page1 = productRepository.searchProducts(condition, pageRequest)
                .map(ProductResponse::from);
        return new ProductPageCache(page1.getContent(), page1.getTotalElements());
    }

    public record ProductPageCache(List<ProductResponse> content, long totalElements) {}

    public ProductCursorResponse findProductListNoOffset(ProductSearchCondition condition, Long lastId, int size) {
        List<Product> results = productRepository.searchProductsNoOffset(condition, lastId, size);

        boolean hasNext = results.size() > size;
        if (hasNext) results = results.subList(0, size);

        Long nextLastId = results.isEmpty() ? null : results.get(results.size() - 1).getId();
        List<ProductResponse> products = results.stream().map(ProductResponse::from).toList();

        return new ProductCursorResponse(products, hasNext, nextLastId);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long productNo) {
        productRepository.deleteById(productNo);
    }
}
