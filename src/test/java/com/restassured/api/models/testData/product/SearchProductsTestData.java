package com.restassured.api.models.testData.product;

public class SearchProductsTestData {
    private String scenario;
    private String searchTerm;
    private int expectedStatusCode;
    private int expectedMinimumProductCount;

    public String getScenario() {
        return scenario;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public int getExpectedMinimumProductCount() {
        return expectedMinimumProductCount;
    }

    @Override
    public String toString() {
        return scenario.replace("_", " ").toLowerCase();
    }
}
