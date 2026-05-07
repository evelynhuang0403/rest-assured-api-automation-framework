package com.restassured.utils.reporting;

import com.restassured.utils.ConfigManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes Allure metadata files that describe the local execution environment and failure categories.
 *
 * <p>Allure reads these files from {@code target/allure-results} when generating the HTML report.
 * Keeping this behavior in code ensures local runs and future CI runs publish the same report context.</p>
 */
public final class AllureEnvironmentWriter {
    private static final String ENVIRONMENT_TEMPLATE_RESOURCE = "allure/environment.properties.template";
    private static final String CATEGORIES_RESOURCE = "allure/categories.json";
    private static final String API_BASE_URL_PLACEHOLDER = "${api.baseUrl}";
    private static final String JAVA_VERSION_PLACEHOLDER = "${java.version}";
    private static final Path ALLURE_RESULTS_DIRECTORY = Path.of("target", "allure-results");
    private static final Path ENVIRONMENT_FILE = ALLURE_RESULTS_DIRECTORY.resolve("environment.properties");
    private static final Path CATEGORIES_FILE = ALLURE_RESULTS_DIRECTORY.resolve("categories.json");

    private AllureEnvironmentWriter() {
    }

    /**
     * Creates the Allure results directory and writes environment and category metadata for the report.
     */
    public static void writeEnvironmentProperties() {
        try {
            Files.createDirectories(ALLURE_RESULTS_DIRECTORY);
            Files.writeString(ENVIRONMENT_FILE, resolvedEnvironmentMetadata(), StandardCharsets.UTF_8);
            copyClasspathResource(CATEGORIES_RESOURCE, CATEGORIES_FILE);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write Allure environment metadata", exception);
        }
    }

    /**
     * Resolves runtime placeholders in the environment metadata template.
     */
    private static String resolvedEnvironmentMetadata() {
        String template = readClasspathResource(ENVIRONMENT_TEMPLATE_RESOURCE);
        String resolved = template
                .replace(API_BASE_URL_PLACEHOLDER, ConfigManager.baseUrl())
                .replace(JAVA_VERSION_PLACEHOLDER, System.getProperty("java.version"));

        if (resolved.contains("${")) {
            throw new IllegalStateException("Unresolved placeholder found in " + ENVIRONMENT_TEMPLATE_RESOURCE);
        }

        return resolved;
    }

    private static String readClasspathResource(String resourcePath) {
        try (InputStream inputStream = resourceStream(resourcePath)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read Allure metadata resource: " + resourcePath, exception);
        }
    }

    private static void copyClasspathResource(String resourcePath, Path outputPath) throws IOException {
        try (InputStream inputStream = resourceStream(resourcePath)) {
            Files.copy(inputStream, outputPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static InputStream resourceStream(String resourcePath) {
        InputStream inputStream = AllureEnvironmentWriter.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException("Required Allure metadata resource not found on classpath: " + resourcePath);
        }
        return inputStream;
    }
}
