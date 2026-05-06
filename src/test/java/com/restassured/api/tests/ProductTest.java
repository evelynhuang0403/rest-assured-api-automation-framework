package com.restassured.api.tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static com.restassured.api.clients.ProductClient.getProductById;
import static com.restassured.api.clients.ProductClient.getProducts;
import static com.restassured.api.clients.ProductClient.searchProducts;
import static com.restassured.api.constants.SchemaPaths.ERROR_SCHEMA;
import static com.restassured.api.constants.SchemaPaths.PRODUCT_SCHEMA;
import static com.restassured.api.tests.assertions.product.ProductSearchAssertions.isProductMatchingSearchTerm;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest extends BaseApiTest {

    private static final int PAGE_LIMIT = 10;
    private static final int PAGE_SKIP = 0;
    private static final int KNOWN_PRODUCT_ID = 1;

    @Test
    @DisplayName("GET /products returns a paginated, well-formed list")
    void getProductsReturnsPaginatedResponse() {
        Response response = getProducts(PAGE_LIMIT, PAGE_SKIP);

        response.then()
                .statusCode(200)
                .body("limit", equalTo(PAGE_LIMIT))
                .body("skip", equalTo(PAGE_SKIP))
                .body("products.size()", equalTo(PAGE_LIMIT))
                .body("total", greaterThanOrEqualTo(PAGE_LIMIT));
    }

    @Test
    @DisplayName("GET /products/{id} returns a product conforming to the product schema")
    void getProductByIdReturnsProductMatchingSchema() {
        getProductById(KNOWN_PRODUCT_ID)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(PRODUCT_SCHEMA))
                .body("id", equalTo(KNOWN_PRODUCT_ID));
    }

    @ParameterizedTest(name = "GET /products/{0} returns 404 for invalid product id")
    @ValueSource(ints = {0, -1, 999999})
    void getProductByIdReturnsNotFoundForInvalidIds(int productId) {
        getProductById(productId)
                .then()
                .statusCode(404)
                .body(matchesJsonSchemaInClasspath(ERROR_SCHEMA))
                .body("message", equalTo("Product with id '" + productId + "' not found"));
    }

    @ParameterizedTest(name = "GET /products/search?q={0} returns products matching the term")
    @ValueSource(strings = {"phone", "watch", "shirt"})
    void searchProductsReturnsMatchingResults(String searchTerm) {
        Response response = searchProducts(searchTerm);

        response.then()
                .statusCode(200)
                .body("products.size()", greaterThan(0))
                .body("total", greaterThan(0));

        List<Map<String, Object>> products = response.jsonPath().getList("products");
        assertTrue(
                products.stream().allMatch(product -> isProductMatchingSearchTerm(product, searchTerm)),
                "Not every result matched search term '" + searchTerm + "'"
        );
    }
}
