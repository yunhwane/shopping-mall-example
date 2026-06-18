package com.example.shoppingmall.catalog.internal

import com.example.shoppingmall.catalog.api.ProductView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/products")
class ProductController(
    private val catalog: CatalogService,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "") keyword: String,
        pageable: Pageable,
    ): Page<ProductView> = catalog.search(keyword, pageable)

    // ponytail: no auth — admin-auth gate arrives with the Member module.
    @PostMapping
    fun create(
        @RequestBody request: CreateProductRequest,
    ): ProductView = catalog.create(request.name, request.price)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: UpdateProductRequest,
    ): ProductView = catalog.update(id, request.name, request.price, request.active)
}

data class CreateProductRequest(
    val name: String,
    val price: BigDecimal,
)

data class UpdateProductRequest(
    val name: String,
    val price: BigDecimal,
    val active: Boolean,
)
