package com.restassured.api.models.request.cart;

import java.util.List;

public class UpdateCartRequest {
    private final boolean merge;
    private final List<CartProductRequest> products;

    public UpdateCartRequest(boolean merge, List<CartProductRequest> products) {
        this.merge = merge;
        this.products = products;
    }

    public boolean isMerge() {
        return merge;
    }

    public List<CartProductRequest> getProducts() {
        return products;
    }
}
