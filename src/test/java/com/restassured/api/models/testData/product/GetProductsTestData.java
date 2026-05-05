package com.restassured.api.models.testData.product;

public class GetProductsTestData {
    private String scenario;
    private int limit;
    private int skip;
    private int expectedStatusCode;
    private int expectedProductCount;

    public String getScenario() {
        return scenario;
    }

    public int getLimit() {
        return limit;
    }

    public int getSkip() {
        return skip;
    }

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public int getExpectedProductCount() {
        return expectedProductCount;
    }

    @Override
    public String toString() {
        return scenario.replace("_", " ").toLowerCase();
    }
}
