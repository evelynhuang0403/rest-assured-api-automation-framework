package com.restassured.api.clients;

import com.restassured.api.models.request.cart.AddCartRequest;
import io.restassured.response.Response;

import static com.restassured.api.constants.endpoints.CartEndpoints.ADD_CART;
import static com.restassured.api.constants.endpoints.CartEndpoints.CARTS_BY_USER;
import static com.restassured.api.constants.endpoints.CartEndpoints.CART_BY_ID;
import static com.restassured.api.config.RequestSpecs.defaultRequestSpec;
import static io.restassured.RestAssured.given;

public final class CartClient {
    private CartClient() {
    }

    public static Response getCart(int cartId) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .pathParam("cartId", cartId)
                .when()
                    .get(CART_BY_ID);
    }

    public static Response getCartsByUser(int userId) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .pathParam("userId", userId)
                .when()
                    .get(CARTS_BY_USER);
    }


    public static Response addCart(AddCartRequest request) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .body(request)
                .when()
                    .post(ADD_CART);
    }
}
