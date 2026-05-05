package com.restassured.api.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.restassured.api.models.testData.AuthLoginTestData;
import com.restassured.api.utils.JsonDataReader;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.restassured.api.clients.AuthClient.getAuthenticatedUser;
import static com.restassured.api.clients.AuthClient.login;
import static com.restassured.api.constants.TestDataPaths.AUTH_LOGIN_TEST_DATA;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class AuthTest extends BaseApiTest {
    /*
        * Reads login test cases from a JSON file and provides them as a stream for parameterized testing.
     */
    static Stream<AuthLoginTestData> loginTestData() {
        return JsonDataReader.readJsonListFromClasspath(
                AUTH_LOGIN_TEST_DATA,
                new TypeReference<List<AuthLoginTestData>>() {}
        ).stream();
    }

    static AuthLoginTestData validLoginTestData() {
        return loginTestData()
                .filter(testData -> testData.getExpectedStatusCode() == 200)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No valid login test data found"));
    }

    @ParameterizedTest(name = "Post /auth/login - {0}")
    @MethodSource("loginTestData")
    void loginReturnsExpectedResponse(AuthLoginTestData testData) {
        var response = login(testData.getUsername(), testData.getPassword())
                .then()
                .statusCode(testData.getExpectedStatusCode());

        if (Objects.equals(testData.getScenario(), "SUCCESSFUL_LOGIN")) {
            response.body("accessToken", notNullValue())
                    .body("username", equalTo(testData.getUsername()));
        }
        else if (Objects.equals(testData.getScenario(), "INVALID_LOGIN")){
            response.body("message", equalTo(testData.getExpectedErrorMessage()));
        }
        else {
            throw new IllegalArgumentException("Unknown test scenario: " + testData.getScenario());
        }
    }

    @Test
    @DisplayName("GET /auth/me returns authenticated user profile")
    void getAuthenticatedUserReturnsUserProfile() {
        AuthLoginTestData validLogin = validLoginTestData();

        Response loginResponse = login(validLogin.getUsername(), validLogin.getPassword());
        String accessToken = loginResponse.jsonPath().getString("accessToken");

        getAuthenticatedUser(accessToken)
        .then()
                .statusCode(200)
                .body("username", equalTo(validLogin.getExpectedUsername()))
                .body("email", equalTo(validLogin.getExpectedEmail()));
    }
}
