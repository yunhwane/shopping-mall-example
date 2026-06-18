package com.example.shoppingmall.catalog

import com.example.shoppingmall.TestcontainersConfiguration
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class CatalogIntegrationTest(
    @Autowired val mvc: MockMvc,
) {
    private fun createProduct(
        name: String,
        price: Int,
    ): Long {
        val body =
            mvc
                .perform(
                    post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"$name","price":$price}"""),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn()
                .response.contentAsString
        return (JsonPath.read(body, "$.id") as Number).toLong()
    }

    @Test
    fun `create, search by name, then update`() {
        val id = createProduct("Mechanical Keyboard", 50000)
        createProduct("Wireless Mouse", 30000)

        // name-only search matches the keyboard, not the mouse
        mvc
            .perform(get("/products").param("keyword", "keyboard"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Mechanical Keyboard"))

        // update price and deactivate
        mvc
            .perform(
                put("/products/$id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Mechanical Keyboard","price":45000,"active":false}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.price").value(45000))
            .andExpect(jsonPath("$.active").value(false))

        // deactivated product drops out of the active-only listing
        mvc
            .perform(get("/products").param("keyword", "keyboard"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(0))
    }

    @Test
    fun `updating a missing product is 404`() {
        mvc
            .perform(
                put("/products/999999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Ghost","price":100,"active":true}"""),
            ).andExpect(status().isNotFound)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `creating with a negative price is 400`() {
        mvc
            .perform(
                post("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Bad","price":-1}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("product price must not be negative"))
    }

    @Test
    fun `creating with a blank name is 400`() {
        mvc
            .perform(
                post("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"   ","price":100}"""),
            ).andExpect(status().isBadRequest)
    }
}
