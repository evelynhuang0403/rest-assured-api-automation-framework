package com.restassured.api.clients;

import com.restassured.api.models.request.AddCartRequest;
import com.restassured.api.models.response.cart.Cart;
import io.restassured.response.Response;

import static com.restassured.api.constants.SchemaPaths.CART_SCHEMA;
import static com.restassured.api.constants.endpoints.CartEndpoints.ADD_CART;
import static com.restassured.api.constants.endpoints.CartEndpoints.CARTS_BY_USER;
import static com.restassured.api.constants.endpoints.CartEndpoints.CART_BY_ID;
import static com.restassured.api.config.RequestSpecs.defaultRequestSpec;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public final class CartClient {
    private CartClient() {
    }

    public static Cart getCart(int cartId) {
        return getCartRaw(cartId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(CART_SCHEMA))
                .extract()
                .as(Cart.class);
    }

    public static Response getCartRaw(int cartId) {
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

    public static Cart addCart(AddCartRequest request) {
        return addCartRaw(request)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath(CART_SCHEMA))
                .extract()
                .as(Cart.class);
    }

    public static Response addCartRaw(AddCartRequest request) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .body(request)
                .when()
                    .post(ADD_CART);
    }
}
