package com.restassured.api.models.testData.product;

public class ProductSearchTestData {
    private String searchTerm;
    private int expectedMinimumProductCount;

    public String getSearchTerm() {
        return searchTerm;
    }

    public int getExpectedMinimumProductCount() {
        return expectedMinimumProductCount;
    }

    @Override
    public String toString() {
        return "search='" + searchTerm + "'";
    }
}
