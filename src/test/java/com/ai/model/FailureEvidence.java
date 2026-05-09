package com.ai.model;

import java.util.List;

public record FailureEvidence(
        String testClassName,
        String testDisplayName,
        String apiDomain,
        String failureType,
        String failureMessage,
        String stackTraceExcerpt,
        long durationMs,
        List<ApiInteraction> interactions
) {
    public FailureEvidence {
        interactions = List.copyOf(interactions == null ? List.of() : interactions);
    }
}
