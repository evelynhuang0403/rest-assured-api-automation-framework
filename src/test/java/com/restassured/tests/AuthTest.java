package com.restassured.tests;

import com.restassured.models.response.auth.AuthenticatedUser;
import com.restassured.models.testdata.auth.AuthTestData;
import com.restassured.models.testdata.auth.InvalidLoginTestData;
import com.restassured.utils.JsonDataReader;
import com.restassured.utils.ConfigManager;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.restassured.api.clients.AuthClient.getAuthenticatedUser;
import static com.restassured.api.clients.AuthClient.login;
import static com.restassured.constants.SchemaPaths.AUTH_LOGIN_SCHEMA;
import static com.restassured.constants.SchemaPaths.AUTH_ME_SCHEMA;
import static com.restassured.constants.SchemaPaths.ERROR_SCHEMA;
import static com.restassured.constants.TestDataPaths.AUTH_TEST_DATA;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
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
        login(ConfigManager.loginUsername(), ConfigManager.loginPassword())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath(AUTH_LOGIN_SCHEMA))
                .body("username", equalTo(ConfigManager.loginUsername()))
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
        JsonPath loginBody = login(ConfigManager.loginUsername(), ConfigManager.loginPassword())
                .then()
                    .statusCode(200)
                .extract()
                    .jsonPath();
        String accessToken = loginBody.getString("accessToken");
        int userIdFromLogin = loginBody.getInt("id");

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(accessToken)
                .then()
                    .statusCode(200)
                    .body(matchesJsonSchemaInClasspath(AUTH_ME_SCHEMA))
                .extract()
                    .as(AuthenticatedUser.class);

        assertThat(authenticatedUser.getId(), equalTo(userIdFromLogin));
        assertThat(authenticatedUser.getUsername(), equalTo(ConfigManager.loginUsername()));
        assertThat(authenticatedUser.getEmail(), equalTo(ConfigManager.loginEmail()));
        assertThat(authenticatedUser.getFirstName(), not(emptyOrNullString()));
        assertThat(authenticatedUser.getLastName(), not(emptyOrNullString()));
        assertThat(authenticatedUser.getImage(), not(emptyOrNullString()));
        assertThat(authenticatedUser.getRole(), equalTo("admin"));
        assertThat(authenticatedUser.getAddress().getCountry(), not(emptyOrNullString()));
        assertThat(authenticatedUser.getAddress().getState(), not(emptyOrNullString()));
        assertThat(authenticatedUser.getCompany().getName(), not(emptyOrNullString()));
        assertThat(authenticatedUser.getCompany().getTitle(), not(emptyOrNullString()));
    }
}
