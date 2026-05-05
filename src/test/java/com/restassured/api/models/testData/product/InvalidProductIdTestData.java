package com.restassured.api.models.testdata.product;

public class InvalidProductIdTestData {
    private int productId;
    private String expectedErrorMessage;

    public int getProductId() {
        return productId;
    }

    public String getExpectedErrorMessage() {
        return expectedErrorMessage;
    }

    @Override
    public String toString() {
        return String.valueOf(productId);
    }
}
