package com.restassured.models.request.cart;

import java.util.List;

public class AddCartRequest {
    private final int userId;
    private final List<CartProductRequest> products;

    public AddCartRequest(int userId, List<CartProductRequest> products) {
        this.userId = userId;
        this.products = products;
    }

    public int getUserId() {
        return userId;
    }

    public List<CartProductRequest> getProducts() {
        return products;
    }
}
