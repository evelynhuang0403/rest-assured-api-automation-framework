package com.restassured.api.config;

public final class ApiConfig {
    private ApiConfig() {
    }

    public static String baseUrl() {
        return System.getProperty("baseUrl", "https://dummyjson.com");
    }
}
