package com.restassured.utils;

import com.restassured.utils.PropertiesReader;

import java.util.Properties;

public final class ConfigManager {
    private static final String CONFIG_FILE = "test-config.properties";
    private static final Properties PROPERTIES = PropertiesReader.readFromClasspath(CONFIG_FILE);

    private ConfigManager() {
    }

    public static String baseUrl() {
        return configValue("baseUrl");
    }

    public static String loginUsername() {
        return configValue("login.username");
    }

    public static String loginPassword() {
        return configValue("login.password");
    }

    public static String loginEmail() {
        return configValue("login.email");
    }

    private static String configValue(String key) {
        return System.getProperty(key, PROPERTIES.getProperty(key));
    }
}
