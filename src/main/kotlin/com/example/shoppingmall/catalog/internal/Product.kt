package com.example.shoppingmall.catalog.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
class Product(
    name: String,
    price: BigDecimal,
    active: Boolean = true,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    // Guards live in the setters, so they run on construction (via the init
    // assignments below) and on every mutation — the entity is the single
    // source of validation truth. Hibernate hydrates fields directly, bypassing
    // them, which is fine: persisted rows already passed validation.
    var name: String = name
        set(value) {
            require(value.isNotBlank()) { "product name must not be blank" }
            field = value
        }

    // numeric(12,2): two decimals, single currency. See ADR-0004.
    @Column(precision = 12, scale = 2)
    var price: BigDecimal = price
        set(value) {
            require(value >= BigDecimal.ZERO) { "product price must not be negative" }
            field = value
        }
    var active: Boolean = active

    init {
        // Re-assign through the setters so construction runs the same guards.
        this.name = name
        this.price = price
    }
}
