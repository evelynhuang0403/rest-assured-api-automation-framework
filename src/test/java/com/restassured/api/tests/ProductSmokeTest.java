package com.restassured.api.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.restassured.api.constants.Endpoints.PRODUCTS;
import static com.restassured.api.constants.Endpoints.PRODUCT_BY_ID;
import static com.restassured.api.specs.RequestSpecs.defaultRequestSpec;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

class ProductSmokeTest extends BaseApiTest {

    @Test
    @DisplayName("GET /products returns paginated products")
    void getProductsReturnsPaginatedProducts() {
        given()
                .spec(defaultRequestSpec())
                .queryParam("limit", 10) // limit the number of products returned to 10
                .queryParam("skip", 0) // start from the first product
        .when()
                .get(PRODUCTS)
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
        given()
                .spec(defaultRequestSpec())
                .pathParam("productId", 1)
        .when()
                .get(PRODUCT_BY_ID)
        .then()
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("Essence Mascara Lash Princess"))
                .body("price", greaterThan(0F))
                .body("category", equalTo("beauty"));
    }
}
