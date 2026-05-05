package com.restassured.api.models.testData.product;

import java.util.List;

public class ProductTestData {
    private List<GetProductsTestData> getProductsTestData;
    private List<GetProductByIdTestData> getProductByIdTestData;
    private List<SearchProductsTestData> searchProductsTestData;

    public List<GetProductsTestData> getGetProductsTestData() {
        return getProductsTestData;
    }

    public List<GetProductByIdTestData> getGetProductByIdTestData() {
        return getProductByIdTestData;
    }

    public List<SearchProductsTestData> getSearchProductsTestData() {
        return searchProductsTestData;
    }
}
