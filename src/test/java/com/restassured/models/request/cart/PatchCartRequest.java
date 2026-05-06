package com.restassured.models.request.cart;

import java.util.List;

public class PatchCartRequest {
    private final List<CartProductRequest> products;

    public PatchCartRequest(List<CartProductRequest> products) {
        this.products = products;
    }

    public List<CartProductRequest> getProducts() {
        return products;
    }
}
