package com.dan.danshop.domain.product.repository;

import com.dan.danshop.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByProductNameContaining(String keyword, Pageable pageable);

    //비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p From Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);
}
