package com.example.shoppingmall

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class ShoppingMallApplicationTests {
    @Test
    fun contextLoads() {
    }
}
