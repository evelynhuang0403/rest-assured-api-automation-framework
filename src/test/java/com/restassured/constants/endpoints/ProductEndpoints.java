package com.restassured.constants.endpoints;

public final class ProductEndpoints {
    private ProductEndpoints() {
    }

    public static final String PRODUCTS = "/products";
    public static final String PRODUCT_BY_ID = "/products/{productId}";
    public static final String SEARCH_PRODUCTS = "/products/search";
    public static final String ADD_PRODUCT = "/products/add";

}
