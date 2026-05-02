package com.restassured.api.clients;

import io.restassured.response.Response;

import static com.restassured.api.specs.RequestSpecs.defaultRequestSpec;
import static io.restassured.RestAssured.given;
import static com.restassured.api.constants.Endpoints.PRODUCTS;
import static com.restassured.api.constants.Endpoints.PRODUCT_BY_ID;

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
}
