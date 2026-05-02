package com.restassured.api.specs;

import com.restassured.api.config.ApiConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecs {
    private RequestSpecs() {
    }

    public static RequestSpecification defaultRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ApiConfig.baseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }
}
