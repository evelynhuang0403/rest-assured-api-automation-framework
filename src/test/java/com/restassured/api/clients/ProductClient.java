package com.restassured.api.clients;

import com.restassured.api.models.request.product.ProductRequest;
import io.restassured.response.Response;

import static com.restassured.api.config.RequestSpecs.defaultRequestSpec;
import static com.restassured.api.constants.endpoints.ProductEndpoints.ADD_PRODUCT;
import static com.restassured.api.constants.endpoints.ProductEndpoints.PRODUCTS;
import static com.restassured.api.constants.endpoints.ProductEndpoints.PRODUCT_BY_ID;
import static com.restassured.api.constants.endpoints.ProductEndpoints.SEARCH_PRODUCTS;
import static io.restassured.RestAssured.given;

public final class ProductClient {
    private ProductClient() {
    }

    public static Response getProducts(int limit, int skip) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .queryParam("limit", limit)
                    .queryParam("skip", skip)
                .when()
                    .get(PRODUCTS);
    }

    public static Response getProductById(int productId) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .pathParam("productId", productId)
                .when()
                    .get(PRODUCT_BY_ID);
    }

    public static Response searchProducts(String query) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .queryParam("q", query)
                .when()
                    .get(SEARCH_PRODUCTS);
    }

    public static Response addProduct(ProductRequest request) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .body(request)
                .when()
                    .post(ADD_PRODUCT);
    }

    public static Response updateProduct(int productId, ProductRequest request) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .pathParam("productId", productId)
                    .body(request)
                .when()
                    .put(PRODUCT_BY_ID);
    }

    public static Response patchProduct(int productId, ProductRequest request) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .pathParam("productId", productId)
                    .body(request)
                .when()
                    .patch(PRODUCT_BY_ID);
    }

    public static Response deleteProduct(int productId) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .pathParam("productId", productId)
                .when()
                    .delete(PRODUCT_BY_ID);
    }
}
