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
    private static final String EXECUTION_ENVIRONMENT_PLACEHOLDER = "${execution.environment}";
    private static final String GIT_BRANCH_PLACEHOLDER = "${git.branch}";
    private static final String GIT_COMMIT_PLACEHOLDER = "${git.commit}";
    private static final String GITHUB_RUN_NUMBER_PLACEHOLDER = "${github.runNumber}";
    private static final String GITHUB_WORKFLOW_PLACEHOLDER = "${github.workflow}";
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
                .replace(JAVA_VERSION_PLACEHOLDER, System.getProperty("java.version"))
                .replace(EXECUTION_ENVIRONMENT_PLACEHOLDER, executionEnvironment())
                .replace(GIT_BRANCH_PLACEHOLDER, firstNonBlank("local", env("GITHUB_HEAD_REF"), env("GITHUB_REF_NAME")))
                .replace(GIT_COMMIT_PLACEHOLDER, firstNonBlank("local", env("GITHUB_SHA")))
                .replace(GITHUB_RUN_NUMBER_PLACEHOLDER, firstNonBlank("local", env("GITHUB_RUN_NUMBER")))
                .replace(GITHUB_WORKFLOW_PLACEHOLDER, firstNonBlank("local", env("GITHUB_WORKFLOW")));

        if (resolved.contains("${")) {
            throw new IllegalStateException("Unresolved placeholder found in " + ENVIRONMENT_TEMPLATE_RESOURCE);
        }

        return resolved;
    }

    private static String executionEnvironment() {
        return "true".equalsIgnoreCase(env("GITHUB_ACTIONS")) ? "GitHub Actions" : "Local";
    }

    private static String env(String key) {
        return System.getenv(key);
    }

    private static String firstNonBlank(String fallback, String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return fallback;
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
