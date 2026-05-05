package com.restassured.api.models.testdata.product;

import java.util.List;

public class ProductTestData {
    private List<InvalidProductIdTestData> invalidProductIdCases;
    private List<ProductSearchTestData> searchCases;

    public List<InvalidProductIdTestData> getInvalidProductIdCases() {
        return invalidProductIdCases;
    }

    public List<ProductSearchTestData> getSearchCases() {
        return searchCases;
    }
}
