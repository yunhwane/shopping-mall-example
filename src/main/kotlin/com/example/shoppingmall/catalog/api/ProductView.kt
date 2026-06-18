package com.example.shoppingmall.catalog.api

import org.springframework.modulith.NamedInterface
import java.math.BigDecimal

// The DTO other modules (e.g. Order) snapshot. Never the entity.
@NamedInterface("api")
data class ProductView(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val active: Boolean,
)
