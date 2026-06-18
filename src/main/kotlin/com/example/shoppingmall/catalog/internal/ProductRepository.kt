package com.example.shoppingmall.catalog.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    // Derived query: parsed/validated at boot, name-only contains search,
    // active products only. Leading-wildcard LIKE is a full scan by design.
    fun findByNameContainingIgnoreCaseAndActiveTrue(
        name: String,
        pageable: Pageable,
    ): Page<Product>
}
