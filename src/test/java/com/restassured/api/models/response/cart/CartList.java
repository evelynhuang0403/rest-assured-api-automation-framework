package com.restassured.api.models.response.cart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

//@JsonIgnoreProperties: Jackson converts JSON into the Class object, ignore any JSON fields that do not exist in the Java class.
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartList {

    private List<Cart> carts;
    private int total;
    private int skip;
    private int limit;

    public List<Cart> getCarts() {
        return carts;
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