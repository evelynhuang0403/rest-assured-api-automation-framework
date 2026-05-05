package com.restassured.api.clients;

import io.restassured.response.Response;

import static com.restassured.api.constants.endpoints.ProductEndpoints.*;
import static com.restassured.api.config.RequestSpecs.defaultRequestSpec;
import static io.restassured.RestAssured.given;

public final class ProductClient {
    private ProductClient() {
    }

    public static Response getProducts(int limit, int skip){
        return
                given()
                    .spec(defaultRequestSpec())
                    .queryParam("limit", limit)
                    .queryParam("skip", skip)
                .when()
                    .get(PRODUCTS);
    }

    public static Response getProductById(int productId){
        return
                given()
                    .spec(defaultRequestSpec())
                    .pathParam("productId", productId)
                .when()
                    .get(PRODUCT_BY_ID);
    }

    public static Response searchProducts(String query){
        return
                given()
                    .spec(defaultRequestSpec())
                    .queryParam("q", query)
                .when()
                    .get(SEARCH_PRODUCTS);
    }
}
