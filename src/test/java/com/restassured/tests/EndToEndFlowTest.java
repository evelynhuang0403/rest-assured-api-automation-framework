package com.restassured.tests;

import com.restassured.api.clients.CartClient;
import com.restassured.utils.ConfigManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.restassured.api.clients.AuthClient.getAuthenticatedUser;
import static com.restassured.api.clients.AuthClient.login;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

class EndToEndFlowTest extends BaseApiTest {

    @Test
    @DisplayName("Login -> /auth/me -> /carts/user/{userId} returns carts owned by the logged-in user")
    void loggedInUserCanFetchTheirOwnCarts() {
        JsonPath loginBody = login(ConfigManager.loginUsername(), ConfigManager.loginPassword())
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
                .body("username", equalTo(ConfigManager.loginUsername()));

        CartClient.getCartsByUser(userIdFromLogin)
                .then()
                .statusCode(200)
                .body("carts.userId", everyItem(equalTo(userIdFromLogin)));
    }
}
