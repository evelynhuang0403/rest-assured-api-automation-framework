package com.restassured.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

public final class JsonDataReader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonDataReader() {
    }

    /**
     * Reads a JSON array from the test classpath and deserializes it into a typed list.
     * Use {@code new TypeReference<List<MyType>>(){}} to preserve generic type at runtime.
     */
    public static <T> List<T> readJsonListFromClasspath(String resourcePath, TypeReference<List<T>> typeReference) {
        try (InputStream inputStream = JsonDataReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Test data file not found: " + resourcePath);
            }

            //convert json to list of objects
            return OBJECT_MAPPER.readValue(inputStream, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read test data file: " + resourcePath, e);
        }
    }

    /**
     * Reads a JSON object from the test classpath and deserializes it into a typed object.
     */
    public static <T> T readJsonObjectFromClasspath(String resourcePath, Class<T> targetType) {
        try (InputStream inputStream = JsonDataReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Test data file not found: " + resourcePath);
            }

            return OBJECT_MAPPER.readValue(inputStream, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read test data file: " + resourcePath, e);
        }
    }
}
