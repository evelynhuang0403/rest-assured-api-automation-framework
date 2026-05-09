package com.restassured.utils;

import com.ai.context.TriageEvidenceContext;
import com.ai.model.FailureEvidence;
import com.ai.model.TriageResult;
import com.ai.service.TriageReportWriter;
import com.ai.service.FailureTriageService;
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
    private static final FailureTriageService failureTriageService = FailureTriageService.createDefault();
    private static final TriageReportWriter triageReportWriter = new TriageReportWriter();

    /**
     * Stores the test start time, resets per-test AI triage evidence, and updates the Allure test display name.
     *
     * @param context JUnit extension context for the test about to execute
     */
    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(NAMESPACE).put(START_TIME_KEY, System.nanoTime());
        TriageEvidenceContext.startTest();
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setName(context.getDisplayName()));
        logger.info("START: {} - {}", testClassName(context), context.getDisplayName());
    }

    /**
     * Logs a successful test result and clears any request/response evidence captured during the test.
     *
     * @param context JUnit extension context for the completed test
     */
    @Override
    public void testSuccessful(ExtensionContext context) {
        logger.info("PASS: {} - {} (duration: {} ms)",
                testClassName(context),
                context.getDisplayName(),
                elapsedMillis(context));

        TriageEvidenceContext.clear();
    }

    /**
     * Logs a failed test, attaches a failure summary, generates AI-assisted triage, and clears evidence state.
     *
     * @param context JUnit extension context for the failed test
     * @param cause exception or assertion error that caused the failure
     */
    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        long durationMs = elapsedMillis(context);
        String failureSummary = failureSummary(context, cause);

        logger.error("FAIL: {} - {} (duration: {} ms). Error: {}",
                testClassName(context),
                context.getDisplayName(),
                durationMs,
                failureMessage(cause),
                cause);

        Allure.addAttachment("Failure Summary", "text/plain", failureSummary, ".txt");
        attachFailureTriage(context, cause, durationMs);
        TriageEvidenceContext.clear();
    }

    /**
     * Builds failure evidence, attaches the generated triage to Allure, and writes markdown triage reports.
     *
     * @param context JUnit extension context for the failed test
     * @param cause exception or assertion error that caused the failure
     * @param durationMs elapsed test duration in milliseconds
     */
    private static void attachFailureTriage(ExtensionContext context, Throwable cause, long durationMs) {
        try {
            // Instantiate the evidence
            FailureEvidence evidence = new FailureEvidence(
                    testClassName(context),
                    context.getDisplayName(),
                    apiDomain(context),
                    cause == null ? "Unknown" : cause.getClass().getName(),
                    failureMessage(cause),
                    stackTrace(cause),
                    durationMs,
                    TriageEvidenceContext.interactions()
            );

            // Generate the triage result using the evidence
            TriageResult triageResult = failureTriageService.triage(evidence);

            // Attach triage as plain text so Allure displays it inline instead of download-only.
            Allure.addAttachment("AI Failure Triage", "text/plain", triageResult.markdown(), ".txt");

            // Write the triage report to the file system for long-term storage
            triageReportWriter.write(evidence, triageResult);

        } catch (Exception exception) {
            logger.warn("AI failure triage could not be generated. Test result will remain unchanged.", exception);

            Allure.addAttachment(
                    "AI Failure Triage",
                    "text/plain",
                    "# AI Failure Triage Unavailable%n%n%s".formatted(exception.getMessage()),
                    ".txt"
            );
        }
    }

    /**
     * Resolves the simple Java test class name for logging, Allure attachments, and triage evidence.
     *
     * @param context JUnit extension context for the current test
     * @return simple test class name, or a fallback when the class is unavailable
     */
    private static String testClassName(ExtensionContext context) {
        return context.getTestClass()
                .map(Class::getSimpleName)
                .orElse("UnknownTestClass");
    }

    /**
     * Calculates elapsed execution time from the timestamp stored before the test started.
     *
     * @param context JUnit extension context containing the per-test store
     * @return elapsed test duration in milliseconds, or zero if the start time is unavailable
     */
    private static long elapsedMillis(ExtensionContext context) {
        Long startTime = context.getStore(NAMESPACE).get(START_TIME_KEY, Long.class);
        if (startTime == null) {
            return 0L;
        }
        return Duration.ofNanos(System.nanoTime() - startTime).toMillis();
    }

    /**
     * Normalizes a failure message so reports always contain a readable value.
     *
     * @param cause exception or assertion error from the failed test
     * @return failure message, or a fallback when the message is blank or unavailable
     */
    private static String failureMessage(Throwable cause) {
        if (cause == null || cause.getMessage() == null || cause.getMessage().isBlank()) {
            return "No failure message provided";
        }
        return cause.getMessage();
    }

    /**
     * Formats the main failure attachment that appears in Allure for failed tests.
     *
     * @param context JUnit extension context for the failed test
     * @param cause exception or assertion error that caused the failure
     * @return plain-text failure summary with metadata and stack trace
     */
    private static String failureSummary(ExtensionContext context, Throwable cause) {
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
                stackTrace(cause)
        );
    }

    /**
     * Converts the thrown failure into a stack trace string for reports and AI triage evidence.
     *
     * @param cause exception or assertion error from the failed test
     * @return stack trace text, or an empty string when no failure is available
     */
    private static String stackTrace(Throwable cause) {
        StringWriter stackTrace = new StringWriter();
        if (cause != null) {
            cause.printStackTrace(new PrintWriter(stackTrace));
        }
        return stackTrace.toString();
    }

    /**
     * Infers the API domain from the test class name for report grouping and triage context.
     *
     * @param context JUnit extension context for the current test
     * @return inferred API domain, or a fallback when the test class is unavailable
     */
    private static String apiDomain(ExtensionContext context) {
        return context.getTestClass()
                .map(Class::getSimpleName)
                .map(className -> className.replace("Test", ""))
                .orElse("Unknown");
    }
}
