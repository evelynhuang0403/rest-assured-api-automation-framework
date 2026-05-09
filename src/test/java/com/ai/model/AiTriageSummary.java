package com.ai.model;

import java.util.List;

public record AiTriageSummary(
        String defectTitle,
        String category,
        String expectedResult,
        String actualResult,
        String suspectedRootCause,
        List<String> reproductionSteps,
        String recommendedNextAction,
        int confidenceScore
) {
    public AiTriageSummary {
        defectTitle = requireText(defectTitle, "defectTitle");
        category = requireText(category, "category");
        expectedResult = requireText(expectedResult, "expectedResult");
        actualResult = requireText(actualResult, "actualResult");
        suspectedRootCause = requireText(suspectedRootCause, "suspectedRootCause");
        reproductionSteps = requireSteps(reproductionSteps);
        recommendedNextAction = requireText(recommendedNextAction, "recommendedNextAction");
        confidenceScore = requireConfidenceScore(confidenceScore);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AI triage summary is missing required field: " + fieldName);
        }
        return value.trim();
    }

    private static List<String> requireSteps(List<String> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("AI triage summary is missing required field: reproductionSteps");
        }

        List<String> normalizedSteps = steps.stream()
                .filter(step -> step != null && !step.isBlank())
                .map(String::trim)
                .toList();

        if (normalizedSteps.isEmpty()) {
            throw new IllegalArgumentException("AI triage summary is missing required field: reproductionSteps");
        }
        return List.copyOf(normalizedSteps);
    }

    private static int requireConfidenceScore(int confidenceScore) {
        if (confidenceScore < 0 || confidenceScore > 100) {
            throw new IllegalArgumentException("AI triage confidenceScore must be between 0 and 100: " + confidenceScore);
        }
        return confidenceScore;
    }
}
