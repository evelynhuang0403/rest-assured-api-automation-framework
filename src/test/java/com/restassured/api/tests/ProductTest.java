package com.restassured.api.tests;

import com.restassured.api.models.testData.product.InvalidProductIdTestData;
import com.restassured.api.models.testData.product.ProductSearchTestData;
import com.restassured.api.models.testData.product.ProductTestData;
import com.restassured.api.utils.JsonDataReader;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.restassured.api.clients.ProductClient.getProductById;
import static com.restassured.api.clients.ProductClient.getProducts;
import static com.restassured.api.clients.ProductClient.searchProducts;
import static com.restassured.api.constants.SchemaPaths.ERROR_SCHEMA;
import static com.restassured.api.constants.SchemaPaths.PRODUCT_SCHEMA;
import static com.restassured.api.constants.TestDataPaths.PRODUCT_TEST_DATA;
import static com.restassured.api.utils.ProductSearchAssertions.isProductMatchingSearchTerm;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest extends BaseApiTest {

    private static final int PAGE_LIMIT = 10;
    private static final int PAGE_SKIP = 0;
    private static final int KNOWN_PRODUCT_ID = 1;

    static ProductTestData productTestData() {
        return JsonDataReader.readJsonObjectFromClasspath(PRODUCT_TEST_DATA, ProductTestData.class);
    }

    static Stream<InvalidProductIdTestData> invalidProductIdCases() {
        return productTestData().getInvalidProductIdCases().stream();
    }

    static Stream<ProductSearchTestData> searchCases() {
        return productTestData().getSearchCases().stream();
    }

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
    @MethodSource("invalidProductIdCases")
    void getProductByIdReturnsNotFoundForInvalidIds(InvalidProductIdTestData testData) {
        getProductById(testData.getProductId())
                .then()
                .statusCode(404)
                .body(matchesJsonSchemaInClasspath(ERROR_SCHEMA))
                .body("message", equalTo(testData.getExpectedErrorMessage()));
    }

    @ParameterizedTest(name = "GET /products/search?q={0} returns products matching the term")
    @MethodSource("searchCases")
    void searchProductsReturnsMatchingResults(ProductSearchTestData testData) {
        Response response = searchProducts(testData.getSearchTerm());

        response.then()
                .statusCode(200)
                .body("products.size()", greaterThanOrEqualTo(testData.getExpectedMinimumProductCount()))
                .body("total", greaterThanOrEqualTo(testData.getExpectedMinimumProductCount()));

        List<Map<String, Object>> products = response.jsonPath().getList("products");
        assertTrue(
                products.stream().allMatch(p -> isProductMatchingSearchTerm(p, testData.getSearchTerm())),
                "Not every result matched search term '" + testData.getSearchTerm() + "'"
        );
    }
}
