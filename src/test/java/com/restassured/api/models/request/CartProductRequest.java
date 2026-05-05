package com.restassured.api.models.request;

public class CartProductRequest {
    private final int id;
    private final int quantity;

    public CartProductRequest(int id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }
}
