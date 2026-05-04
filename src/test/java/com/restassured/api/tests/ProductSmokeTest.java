package com.restassured.api.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import static com.restassured.api.clients.ProductClient.getProductById;
import static com.restassured.api.clients.ProductClient.getProducts;
import static com.restassured.api.constants.SchemaPaths.PRODUCT_SCHEMA;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
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
                .body("price", greaterThan(0F))
                .body(matchesJsonSchemaInClasspath(PRODUCT_SCHEMA));
    }

    @ParameterizedTest(name = "GET /products/{0} returns 404 for invalid product id")
    @ValueSource(ints = {999999, -1, 0})
    void getProductByIdReturnsNotFoundForNonExistingProduct(int productId) {
        getProductById(productId)
        .then()
                .assertThat()
                .statusCode(404)
                .body("message", equalTo("Product with id '" + productId + "' not found"));
    }
}
