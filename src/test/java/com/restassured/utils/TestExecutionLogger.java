package com.restassured.utils;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;

public class TestExecutionLogger implements BeforeTestExecutionCallback, TestWatcher {
    private static final Logger logger = LoggerFactory.getLogger(TestExecutionLogger.class);
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(TestExecutionLogger.class);
    private static final String START_TIME_KEY = "startTime";

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(NAMESPACE).put(START_TIME_KEY, System.nanoTime());
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setName(context.getDisplayName()));
        logger.info("START: {} - {}", testClassName(context), context.getDisplayName());
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        logger.info("PASS: {} - {} (duration: {} ms)",
                testClassName(context),
                context.getDisplayName(),
                elapsedMillis(context));
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        logger.error("FAIL: {} - {} (duration: {} ms). Error: {}",
                testClassName(context),
                context.getDisplayName(),
                elapsedMillis(context),
                failureMessage(cause),
                cause);
        Allure.addAttachment("Failure Summary", "text/plain", failureSummary(context, cause), ".txt");
    }

    private static String testClassName(ExtensionContext context) {
        return context.getTestClass()
                .map(Class::getSimpleName)
                .orElse("UnknownTestClass");
    }

    private static long elapsedMillis(ExtensionContext context) {
        Long startTime = context.getStore(NAMESPACE).get(START_TIME_KEY, Long.class);
        if (startTime == null) {
            return 0L;
        }
        return Duration.ofNanos(System.nanoTime() - startTime).toMillis();
    }

    private static String failureMessage(Throwable cause) {
        if (cause == null || cause.getMessage() == null || cause.getMessage().isBlank()) {
            return "No failure message provided";
        }
        return cause.getMessage();
    }

    private static String failureSummary(ExtensionContext context, Throwable cause) {
        StringWriter stackTrace = new StringWriter();
        if (cause != null) {
            cause.printStackTrace(new PrintWriter(stackTrace));
        }

        return """
                Test class: %s
                Test name: %s
                Failure type: %s
                Failure message: %s

                Stack trace:
                %s
                """.formatted(
                testClassName(context),
                context.getDisplayName(),
                cause == null ? "Unknown" : cause.getClass().getName(),
                failureMessage(cause),
                stackTrace
        );
    }
}
