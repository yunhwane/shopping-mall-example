package com.example.shoppingmall.catalog

import com.example.shoppingmall.catalog.internal.Product
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal

class ProductSpec :
    StringSpec({
        "rejects a blank name on construction" {
            shouldThrow<IllegalArgumentException> { Product("  ", BigDecimal.TEN) }
        }

        "rejects a negative price on construction" {
            shouldThrow<IllegalArgumentException> { Product("Keyboard", BigDecimal("-1")) }
        }

        "mutation applies new values" {
            val product = Product("Keyboard", BigDecimal("50000"))
            product.name = "Mouse"
            product.price = BigDecimal("30000")
            product.active = false
            product.name shouldBe "Mouse"
            product.price shouldBe BigDecimal("30000")
            product.active shouldBe false
        }

        "mutation re-validates" {
            val product = Product("Keyboard", BigDecimal("50000"))
            shouldThrow<IllegalArgumentException> { product.name = "" }
        }
    })
