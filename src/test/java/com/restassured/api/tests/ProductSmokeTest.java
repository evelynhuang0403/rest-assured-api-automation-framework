package com.restassured.api.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.restassured.api.clients.ProductClient.getProductById;
import static com.restassured.api.clients.ProductClient.getProducts;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

class ProductSmokeTest extends BaseApiTest {

    @Test
    @DisplayName("GET /products returns paginated products")
    void getProductsReturnsPaginatedProducts() {
        getProducts(10, 0)
        .then()
                .assertThat()
                .statusCode(200)
                .body("products.size()", equalTo(10))
                .body("products[0].id", greaterThan(0))
                .body("products[0].title", notNullValue())
                .body("total", greaterThan(10))
                .body("limit", equalTo(10))
                .body("skip", equalTo(0));
    }

    @Test
    @DisplayName("GET /products/{id} returns product details")
    void getProductByIdReturnsSingleProduct() {
        getProductById(1)
        .then()
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("Essence Mascara Lash Princess"))
                .body("price", greaterThan(0F))
                .body("category", equalTo("beauty"));
    }
}
