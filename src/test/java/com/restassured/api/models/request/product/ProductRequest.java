package com.restassured.api.models.request.product;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductRequest {
    private final String title;
    private final String description;
    private final String category;
    private final Float price;
    private final Integer stock;
    private final String brand;

    public ProductRequest(String title, String description, String category, Float price, Integer stock, String brand) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.brand = brand;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Float getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getBrand() {
        return brand;
    }
}
