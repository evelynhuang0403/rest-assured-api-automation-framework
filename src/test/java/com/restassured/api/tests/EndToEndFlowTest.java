package com.restassured.api.tests;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.restassured.api.clients.AuthClient.getAuthenticatedUser;
import static com.restassured.api.clients.AuthClient.login;
import static com.restassured.api.clients.CartClient.getCartsByUser;
import static com.restassured.api.fixtures.TestUsers.VALID_PASSWORD;
import static com.restassured.api.fixtures.TestUsers.VALID_USERNAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

class EndToEndFlowTest extends BaseApiTest {

    @Test
    @DisplayName("Login -> /auth/me -> /carts/user/{userId} returns carts owned by the logged-in user")
    void loggedInUserCanFetchTheirOwnCarts() {
        JsonPath loginBody = login(VALID_USERNAME, VALID_PASSWORD)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        String accessToken = loginBody.getString("accessToken");
        int userIdFromLogin = loginBody.getInt("id");

        Response profile = getAuthenticatedUser(accessToken);
        profile.then()
                .statusCode(200)
                .body("id", equalTo(userIdFromLogin))
                .body("username", equalTo(VALID_USERNAME));

        getCartsByUser(userIdFromLogin)
                .then()
                .statusCode(200)
                .body("carts.userId", everyItem(equalTo(userIdFromLogin)));
    }
}
