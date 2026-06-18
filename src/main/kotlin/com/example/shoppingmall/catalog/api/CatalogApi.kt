package com.example.shoppingmall.catalog.api

import org.springframework.modulith.NamedInterface

// The only catalog type other modules may call. active-agnostic: returns
// deactivated products too, so callers decide for themselves.
@NamedInterface("api")
interface CatalogApi {
    fun findProduct(id: Long): ProductView?
}
