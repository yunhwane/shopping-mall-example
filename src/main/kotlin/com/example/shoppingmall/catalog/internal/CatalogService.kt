package com.example.shoppingmall.catalog.internal

import com.example.shoppingmall.catalog.api.CatalogApi
import com.example.shoppingmall.catalog.api.ProductView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class CatalogService(
    private val products: ProductRepository,
) : CatalogApi {
    override fun findProduct(id: Long): ProductView? = products.findById(id).orElse(null)?.toView()

    fun search(
        keyword: String,
        pageable: Pageable,
    ): Page<ProductView> = products.findByNameContainingIgnoreCaseAndActiveTrue(keyword, pageable).map { it.toView() }

    @Transactional
    fun create(
        name: String,
        price: BigDecimal,
    ): ProductView = products.save(Product(name, price)).toView()

    @Transactional
    fun update(
        id: Long,
        name: String,
        price: BigDecimal,
        active: Boolean,
    ): ProductView {
        val product =
            products.findById(id).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "product $id not found")
            }
        product.name = name
        product.price = price
        product.active = active
        return product.toView()
    }
}

private fun Product.toView() = ProductView(id, name, price, active)
