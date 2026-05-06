package com.restassured.utils;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestExecutionLogger implements BeforeTestExecutionCallback, TestWatcher {
    private static final Logger logger = LoggerFactory.getLogger(TestExecutionLogger.class);

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        logger.info("START: {}", context.getDisplayName());
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        logger.info("PASS: {}", context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        logger.error("FAIL: {}", context.getDisplayName());
    }
}
