package com.ai.service;

import com.ai.config.AiTriageConfig;
import com.ai.model.FailureEvidence;
import com.ai.model.TriageResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * Writes generated triage markdown to per-failure files and an aggregate report
 * that can be uploaded as a CI artifact.
 */
public class TriageReportWriter {
    private static final Path REPORT_DIRECTORY = AiTriageConfig.reportDirectory();
    private static final Path AGGREGATE_REPORT = REPORT_DIRECTORY.resolve("failure-triage-report.md");

    /**
     * Writes one per-failure markdown file and appends the same content to the aggregate report.
     *
     * @param evidence failed-test evidence used to name and describe the report
     * @param result generated triage result to persist
     * @throws IOException when the report directory or markdown files cannot be written
     */
    public synchronized void write(FailureEvidence evidence, TriageResult result) throws IOException {
        Files.createDirectories(REPORT_DIRECTORY);
        String fileName = safeFileName(evidence.testClassName() + "-" + evidence.testDisplayName())
                + "-" + Instant.now().toEpochMilli() + ".md";
        Path failureReport = REPORT_DIRECTORY.resolve(fileName);
        String content = reportContent(evidence, result);

        Files.writeString(failureReport, content, StandardCharsets.UTF_8);
        Files.writeString(
                AGGREGATE_REPORT,
                content + System.lineSeparator() + System.lineSeparator() + "---" + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    /**
     * Combines triage metadata and generated markdown into the persisted report body.
     *
     * @param evidence failed-test evidence summarized in the report header
     * @param result generated triage result
     * @return complete markdown report content
     */
    private static String reportContent(FailureEvidence evidence, TriageResult result) {
        return """
                # AI Failure Triage Report

                - Test class: %s
                - Test display name: %s
                - API domain: %s
                - Triage source: %s
                - AI attempted: %s
                - AI succeeded: %s

                %s
                """.formatted(
                evidence.testClassName(),
                evidence.testDisplayName(),
                evidence.apiDomain(),
                result.source(),
                result.aiAttempted(),
                result.aiSucceeded(),
                result.markdown()
        ).trim();
    }

    /**
     * Converts a test name into a filesystem-safe markdown filename stem.
     *
     * @param value raw test class and display name
     * @return sanitized filename stem
     */
    private static String safeFileName(String value) {
        return value == null
                ? "unknown-test"
                : value.replaceAll("[^A-Za-z0-9._-]+", "-")
                        .replaceAll("-+", "-")
                        .replaceAll("(^-|-$)", "");
    }
}
