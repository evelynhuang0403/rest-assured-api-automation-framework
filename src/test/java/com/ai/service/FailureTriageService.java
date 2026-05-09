package com.ai.service;

import com.ai.client.OpenAiTriageClient;
import com.ai.config.AiTriageConfig;
import com.ai.model.AiTriageSummary;
import com.ai.model.FailureEvidence;
import com.ai.model.TriageResult;

/**
 * Orchestrates failure triage by using OpenAI when configured and reporting
 * a transparent unavailable result whenever AI cannot produce valid triage.
 */
public class FailureTriageService {
    private final OpenAiTriageClient openAiTriageClient;
    private final TriageMarkdownRenderer triageMarkdownRenderer;

    /**
     * Creates a service with injectable collaborators for easier testing and reuse.
     *
     * @param openAiTriageClient client used for live model-backed triage
     * @param triageMarkdownRenderer renderer used to produce stable report markdown
     */
    public FailureTriageService(
            OpenAiTriageClient openAiTriageClient,
            TriageMarkdownRenderer triageMarkdownRenderer
    ) {
        this.openAiTriageClient = openAiTriageClient;
        this.triageMarkdownRenderer = triageMarkdownRenderer;
    }

    /**
     * Builds the production triage service used by the JUnit execution logger.
     *
     * @return service wired with the default OpenAI client and markdown renderer
     */
    public static FailureTriageService createDefault() {
        return new FailureTriageService(
                new OpenAiTriageClient(),
                new TriageMarkdownRenderer()
        );
    }

    /**
     * Generates triage for one failed test, using OpenAI when configured.
     *
     * @param evidence failure evidence collected from JUnit and REST Assured
     * @return triage result including markdown, source, and AI attempt metadata
     */
    public TriageResult triage(FailureEvidence evidence) {
        String apiKey = AiTriageConfig.apiKey();

        if (apiKey.isBlank()) {
            return unavailable("ai.triage.apiKey is not configured.", evidence, false);
        }

        try {
            AiTriageSummary summary = openAiTriageClient.generateTriage(evidence, apiKey);
            String markdown = triageMarkdownRenderer.render(summary);
            return new TriageResult(markdown, "OpenAI Responses API (" + AiTriageConfig.model() + ")", true, true);
        } catch (RuntimeException exception) {
            return unavailable("OpenAI triage failed: " + exception.getMessage(), evidence, true);
        }
    }

    /**
     * Builds an unavailable result with the reason live AI triage was not generated.
     *
     * @param reason user-facing explanation for unavailable AI triage
     * @param evidence failed-test evidence used for identifying the test only
     * @param aiAttempted whether a live AI call was attempted
     * @return unavailable triage result
     */
    private TriageResult unavailable(String reason, FailureEvidence evidence, boolean aiAttempted) {
        return new TriageResult(unavailableMarkdown(reason, evidence), "AI triage unavailable", aiAttempted, false);
    }

    /**
     * Formats a clear non-triage message when AI cannot provide a valid result.
     *
     * @param reason user-facing explanation for unavailable AI triage
     * @param evidence failed-test evidence used for identifying the test only
     * @return markdown explaining why no AI-generated triage is available
     */
    private static String unavailableMarkdown(String reason, FailureEvidence evidence) {
        return """
                # AI Failure Triage Unavailable

                AI-generated triage was not produced for this failure.

                ## Reason

                %s

                ## Failed Test

                - Test class: %s
                - Test display name: %s
                - API domain: %s
                """.formatted(
                reason,
                evidence.testClassName(),
                evidence.testDisplayName(),
                evidence.apiDomain()
        ).trim();
    }
}
