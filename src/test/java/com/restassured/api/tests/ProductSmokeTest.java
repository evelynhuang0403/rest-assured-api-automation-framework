package com.restassured.api.tests;

import com.restassured.api.models.testData.product.GetProductByIdTestData;
import com.restassured.api.models.testData.product.GetProductsTestData;
import com.restassured.api.models.testData.product.ProductTestData;
import com.restassured.api.models.testData.product.SearchProductsTestData;
import com.restassured.api.utils.JsonDataReader;
import io.restassured.response.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.restassured.api.constants.TestDataPaths.PRODUCT_SMOKE_TEST_DATA;
import static com.restassured.api.constants.SchemaPaths.PRODUCT_SCHEMA;
import static com.restassured.api.utils.ProductSearchAssertions.isProductMatchingSearchTerm;
import static com.restassured.api.clients.ProductClient.getProductById;
import static com.restassured.api.clients.ProductClient.getProducts;
import static com.restassured.api.clients.ProductClient.searchProducts;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductSmokeTest extends BaseApiTest {

    static ProductTestData productTestData() {
        return JsonDataReader.readJsonObjectFromClasspath(PRODUCT_SMOKE_TEST_DATA, ProductTestData.class);
    }

    static Stream<GetProductsTestData> getProductsTestData() {
        return productTestData().getGetProductsTestData().stream();
    }

    static Stream<GetProductByIdTestData> getProductByIdSuccessTestData() {
        return productTestData().getGetProductByIdTestData().stream()
                .filter(testData -> testData.getExpectedStatusCode() == 200);
    }

    static Stream<GetProductByIdTestData> getProductByIdNotFoundTestData() {
        return productTestData().getGetProductByIdTestData().stream()
                .filter(testData -> testData.getExpectedStatusCode() == 404);
    }

    static Stream<SearchProductsTestData> searchProductsTestData() {
        return productTestData().getSearchProductsTestData().stream();
    }

    @ParameterizedTest(name = "GET /products - {0}")
    @MethodSource("getProductsTestData")
    void getProductsReturnsPaginatedProducts(GetProductsTestData testData) {
        getProducts(testData.getLimit(), testData.getSkip())
        .then()
                .assertThat()
                .statusCode(testData.getExpectedStatusCode())
                .body("products.size()", equalTo(testData.getExpectedProductCount()))
                .body("products[0].id", greaterThan(0))
                .body("products[0].title", notNullValue())
                .body("total", greaterThan(testData.getExpectedProductCount()))
                .body("limit", equalTo(testData.getLimit()))
                .body("skip", equalTo(testData.getSkip()));
    }

    @ParameterizedTest(name = "GET /products/{0} returns product details")
    @MethodSource("getProductByIdSuccessTestData")
    void getProductByIdReturnsSingleProduct(GetProductByIdTestData testData) {
        getProductById(testData.getProductId())
        .then()
                .assertThat()
                .statusCode(testData.getExpectedStatusCode())
                .body("id", equalTo(testData.getExpectedProductId()))
                .body("price", greaterThan(0F))
                .body(matchesJsonSchemaInClasspath(PRODUCT_SCHEMA));
    }

    @ParameterizedTest(name = "GET /products/search returns matching products")
    @MethodSource("searchProductsTestData")
    void searchProductsReturnsMatchingResults(SearchProductsTestData testData){
        Response response = searchProducts(testData.getSearchTerm());
        response.then()
                    .assertThat()
                    .statusCode(testData.getExpectedStatusCode())
                    .body("products.size()", greaterThanOrEqualTo(testData.getExpectedMinimumProductCount()))
                    .body("total", greaterThanOrEqualTo(testData.getExpectedMinimumProductCount()));

        List<Map<String, Object>> products = response.jsonPath().getList("products");
        assertTrue(products.stream().allMatch(
                product -> isProductMatchingSearchTerm(product, testData.getSearchTerm())),
                "Not all products in the search results match the search term '" + testData.getSearchTerm() + "'");
    }

    @ParameterizedTest(name = "GET /products by id returns 404 - invalid product id={0}")
    @MethodSource("getProductByIdNotFoundTestData")
    void getProductByIdReturnsNotFoundForNonExistingProduct(GetProductByIdTestData testData) {
        getProductById(testData.getProductId())
        .then()
                .assertThat()
                .statusCode(testData.getExpectedStatusCode())
                .body("message", equalTo(testData.getExpectedErrorMessage()));
    }
}
