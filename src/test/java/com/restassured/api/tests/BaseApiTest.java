package com.restassured.api.tests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseApiTest {

    @BeforeAll
    static void enableFailureLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
