package com.restassured.api.models.testData.product;

public class GetProductByIdTestData {
    private String scenario;
    private int productId;
    private int expectedStatusCode;
    private int expectedProductId;
    private String expectedErrorMessage;

    public String getScenario() {
        return scenario;
    }

    public int getProductId() {
        return productId;
    }

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public int getExpectedProductId() {
        return expectedProductId;
    }

    public String getExpectedErrorMessage() {
        return expectedErrorMessage;
    }

    @Override
    public String toString() {
        return String.valueOf(productId);
    }
}
