package com.restassured.api.models.testData;

public class AuthLoginTestData {
    private String scenario;
    private String username;
    private String password;
    private int expectedStatusCode;
    private String expectedUsername;
    private String expectedEmail;
    private String expectedErrorMessage;

    public String getScenario() {
        return scenario;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getExpectedEmail() { return expectedEmail;}

    public int getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public String getExpectedUsername() {
        return expectedUsername;
    }

    public String getExpectedErrorMessage() {
        return expectedErrorMessage;
    }

    @Override
    public String toString() {
        return scenario.replace("_", " ").toLowerCase();
    }
}
