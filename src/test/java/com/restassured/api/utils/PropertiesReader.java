package com.restassured.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesReader {
    private PropertiesReader() {
    }

    public static Properties readFromClasspath(String resourcePath) {
        Properties properties = new Properties();

        try (InputStream inputStream = PropertiesReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Required properties file not found on classpath: " + resourcePath);
            }

            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load properties file: " + resourcePath, exception);
        }

        return properties;
    }
}
