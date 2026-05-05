package com.restassured.api.tests;

import com.restassured.api.config.ResponseSpecs;
import com.restassured.api.utils.TestExecutionLogger;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestExecutionLogger.class)
public abstract class BaseApiTest {

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.responseSpecification = ResponseSpecs.defaultResponseSpec();
    }
}
