package com.ai.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads AI triage settings from the ignored local properties file and exposes
 * safe defaults when optional settings are absent.
 */
public final class AiTriageConfig {
    private static final String CONFIG_FILE = "ai-triage.properties";
    private static final Properties PROPERTIES = loadProperties();
    private static final String API_KEY = "ai.triage.apiKey";
    private static final String MODEL_KEY = "ai.triage.model";
    private static final String TIMEOUT_SECONDS_KEY = "ai.triage.timeout.seconds";
    private static final String MAX_RETRIES_KEY = "ai.triage.maxRetries";
    private static final String MAX_OUTPUT_TOKENS_KEY = "ai.triage.maxOutputTokens";
    private static final String REPORT_DIRECTORY_KEY = "ai.triage.reportDirectory";

    /**
     * Prevents instantiation because this class exposes static configuration accessors only.
     */
    private AiTriageConfig() {
    }

    /**
     * Returns the OpenAI API key configured in the ignored local properties file.
     *
     * @return configured API key, or an empty string when live AI triage is not enabled
     */
    public static String apiKey() {
        return configValue(API_KEY, "");
    }

    /**
     * Returns the model name used for live AI triage.
     *
     * @return configured model name, or the framework default when not set
     */
    public static String model() {
        return configValue(MODEL_KEY, "gpt-5-mini");
    }

    /**
     * Returns the OpenAI client timeout in seconds.
     *
     * @return configured timeout, or the framework default when not set
     */
    public static int timeoutSeconds() {
        return intConfigValue(TIMEOUT_SECONDS_KEY, 45);
    }

    /**
     * Returns the maximum number of retries allowed for the OpenAI client.
     *
     * @return configured retry count, or the framework default when not set
     */
    public static int maxRetries() {
        return intConfigValue(MAX_RETRIES_KEY, 1);
    }

    /**
     * Returns the maximum response size requested from the model.
     *
     * @return configured output-token limit, or the framework default when not set
     */
    public static long maxOutputTokens() {
        return intConfigValue(MAX_OUTPUT_TOKENS_KEY, 3000);
    }

    /**
     * Returns the directory where generated triage markdown files are written.
     *
     * @return configured report directory, or the framework default when not set
     */
    public static Path reportDirectory() {
        return Path.of(configValue(REPORT_DIRECTORY_KEY, "target/ai-triage"));
    }

    /**
     * Resolves a string setting from system properties, local properties, or a default.
     *
     * @param key property key to resolve
     * @param defaultValue value to use when the property is absent
     * @return trimmed configuration value
     */
    private static String configValue(String key, String defaultValue) {
        return System.getProperty(key, PROPERTIES.getProperty(key, defaultValue)).trim();
    }

    /**
     * Resolves and parses an integer setting.
     *
     * @param key property key to resolve
     * @param defaultValue value to use when the property is absent
     * @return parsed integer configuration value
     */
    private static int intConfigValue(String key, int defaultValue) {
        String configuredValue = configValue(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(configuredValue);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid integer value for config key '" + key + "': " + configuredValue, exception);
        }
    }

    /**
     * Loads the ignored local AI triage properties file when present.
     *
     * @return loaded properties, or an empty set when the local file is absent
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = AiTriageConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load properties file: " + CONFIG_FILE, exception);
        }
        return properties;
    }
}
