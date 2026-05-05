package com.restassured.api.models.response.cart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cart {
    private int id;
    private int userId;
    private List<CartProduct> products;
    private float total;
    private float discountedTotal;
    private int totalProducts;
    private int totalQuantity;

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public List<CartProduct> getProducts() {
        return products;
    }

    public float getTotal() {
        return total;
    }

    public float getDiscountedTotal() {
        return discountedTotal;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }
}
