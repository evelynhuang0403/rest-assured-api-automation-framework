package com.restassured.api.constants.endpoints;

public final class CartEndpoints {
    private CartEndpoints() {
    }

    public static final String CART_BY_ID = "/carts/{cartId}";
    public static final String CARTS_BY_USER = "/carts/user/{userId}";
    public static final String ADD_CART = "/carts/add";
}
