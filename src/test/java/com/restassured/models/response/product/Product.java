package com.restassured.models.response.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private int id;
    private String title;
    private String description;
    private String category;
    private float price;
    private int stock;
    private String brand;
    @JsonProperty("isDeleted")
    private boolean isDeleted;
    private String deletedOn;

    public int getId() {
        return id;
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

    public float getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getBrand() {
        return brand;
    }
}
