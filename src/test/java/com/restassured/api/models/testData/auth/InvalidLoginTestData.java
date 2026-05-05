package com.restassured.api.models.testdata.auth;

public class InvalidLoginTestData {
    private String description;
    private String username;
    private String password;
    private String expectedErrorMessage;

    public String getDescription() {
        return description;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getExpectedErrorMessage() {
        return expectedErrorMessage;
    }

    @Override
    public String toString() {
        return description;
    }
}
