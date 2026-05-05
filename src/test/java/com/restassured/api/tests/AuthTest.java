package com.restassured.api.tests;

import com.restassured.api.models.testData.auth.AuthTestData;
import com.restassured.api.models.testData.auth.InvalidLoginTestData;
import com.restassured.api.utils.JsonDataReader;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.restassured.api.clients.AuthClient.getAuthenticatedUser;
import static com.restassured.api.clients.AuthClient.login;
import static com.restassured.api.constants.SchemaPaths.AUTH_LOGIN_SCHEMA;
import static com.restassured.api.constants.SchemaPaths.ERROR_SCHEMA;
import static com.restassured.api.constants.TestDataPaths.AUTH_TEST_DATA;
import static com.restassured.api.fixtures.TestUsers.VALID_EMAIL;
import static com.restassured.api.fixtures.TestUsers.VALID_PASSWORD;
import static com.restassured.api.fixtures.TestUsers.VALID_USERNAME;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class AuthTest extends BaseApiTest {

    static Stream<InvalidLoginTestData> invalidLoginCases() {
        return JsonDataReader.readJsonObjectFromClasspath(AUTH_TEST_DATA, AuthTestData.class)
                .getInvalidLoginCases()
                .stream();
    }

    @Test
    @DisplayName("POST /auth/login returns a token and profile matching the login schema")
    void loginWithValidCredentialsReturnsToken() {
        login(VALID_USERNAME, VALID_PASSWORD)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(AUTH_LOGIN_SCHEMA))
                .body("username", equalTo(VALID_USERNAME))
                .body("accessToken", notNullValue());
    }

    @ParameterizedTest(name = "POST /auth/login returns 400 - {0}")
    @MethodSource("invalidLoginCases")
    void loginWithInvalidCredentialsReturns400(InvalidLoginTestData testData) {
        login(testData.getUsername(), testData.getPassword())
                .then()
                .statusCode(400)
                .body(matchesJsonSchemaInClasspath(ERROR_SCHEMA))
                .body("message", equalTo(testData.getExpectedErrorMessage()));
    }

    @Test
    @DisplayName("GET /auth/me returns the profile of the authenticated user")
    void getAuthenticatedUserReturnsProfile() {
        Response loginResponse = login(VALID_USERNAME, VALID_PASSWORD);
        String accessToken = loginResponse.jsonPath().getString("accessToken");

        getAuthenticatedUser(accessToken)
                .then()
                .statusCode(200)
                .body("username", equalTo(VALID_USERNAME))
                .body("email", equalTo(VALID_EMAIL));
    }
}
