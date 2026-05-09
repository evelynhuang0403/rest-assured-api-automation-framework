package com.ai.service;

import com.ai.model.AiTriageSummary;

/**
 * Renders structured AI triage summaries into a stable markdown format for Allure and CI artifacts.
 */
public class TriageMarkdownRenderer {
    /**
     * Converts a structured triage summary into the report format used by every result surface.
     *
     * @param summary structured triage summary produced by OpenAI
     * @return markdown report body with stable section ordering
     */
    public String render(AiTriageSummary summary) {
        return """
                # Failure Triage Summary

                ## Defect Title

                %s

                ## Category

                %s

                ## Expected Result

                %s

                ## Actual Result

                %s

                ## Suspected Root Cause

                %s

                ## Reproduction Steps

                %s

                ## Recommended Next Debugging Action

                %s

                ## Confidence Score

                %d%%
                """.formatted(
                summary.defectTitle(),
                summary.category(),
                summary.expectedResult(),
                summary.actualResult(),
                summary.suspectedRootCause(),
                formattedSteps(summary),
                summary.recommendedNextAction(),
                summary.confidenceScore()
        ).trim();
    }

    /**
     * Formats reproduction steps as a markdown numbered list.
     *
     * @param summary structured triage summary containing reproduction steps
     * @return numbered markdown list
     */
    private static String formattedSteps(AiTriageSummary summary) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < summary.reproductionSteps().size(); index++) {
            builder.append(index + 1)
                    .append(". ")
                    .append(summary.reproductionSteps().get(index))
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }
}
