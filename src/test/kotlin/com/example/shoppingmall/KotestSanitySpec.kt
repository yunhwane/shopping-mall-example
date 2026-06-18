package com.example.shoppingmall

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

// ponytail: proves the kotest JUnit5 runner is wired; delete once real specs exist.
class KotestSanitySpec :
    StringSpec({
        "kotest runner works" {
            (1 + 1) shouldBe 2
        }
    })
