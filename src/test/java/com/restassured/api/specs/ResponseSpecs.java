package com.restassured.api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.Matchers.lessThan;

public final class ResponseSpecs {
    private static final long DEFAULT_RESPONSE_TIME_SLA_MS = 5000L;

    private ResponseSpecs() {
    }

    public static ResponseSpecification defaultResponseSpec() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(DEFAULT_RESPONSE_TIME_SLA_MS))
                .build();
    }
}
