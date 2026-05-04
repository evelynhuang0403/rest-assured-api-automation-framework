package com.restassured.api.tests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import com.restassured.api.utils.TestExecutionLogger;

@ExtendWith(TestExecutionLogger.class)
public abstract class BaseApiTest {

    @BeforeAll
    static void enableFailureLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
