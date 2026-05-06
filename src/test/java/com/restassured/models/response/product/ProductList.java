package com.restassured.models.response.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductList {
    private List<Product> products;
    private int total;
    private int skip;
    private int limit;

    public List<Product> getProducts() {
        return products;
    }

    public int getTotal() {
        return total;
    }

    public int getSkip() {
        return skip;
    }

    public int getLimit() {
        return limit;
    }
}
