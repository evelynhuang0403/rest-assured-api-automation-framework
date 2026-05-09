package com.ai.model;

public record TriageResult(
        String markdown,
        String source,
        boolean aiAttempted,
        boolean aiSucceeded
) {
}
