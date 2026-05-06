package com.restassured.api.clients;

import com.restassured.api.models.request.auth.LoginRequest;
import io.restassured.response.Response;

import static com.restassured.api.constants.endpoints.AuthEndpoints.AUTH_LOGIN;
import static com.restassured.api.constants.endpoints.AuthEndpoints.AUTH_ME;
import static com.restassured.api.config.RequestSpecs.defaultRequestSpec;
import static io.restassured.RestAssured.given;

public final class AuthClient {
    private AuthClient() {
    }

    public static Response login(String username, String password) {
        return
                given()
                    .spec(defaultRequestSpec())
                    .body(new LoginRequest(username, password))
                .when()
                    .post(AUTH_LOGIN);
    }

    public static Response getAuthenticatedUser(String accessToken) {
        return given()
                    .spec(defaultRequestSpec())
                    .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(AUTH_ME);
    }
}
