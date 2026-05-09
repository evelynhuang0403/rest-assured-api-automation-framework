package com.ai.model;

public record ApiInteraction(
        String method,
        String uri,
        String queryParameters,
        String requestHeaders,
        String requestBody,
        int responseStatus,
        long responseTimeMs,
        String responseHeaders,
        String responseBody,
        String requestAttachment,
        String responseAttachment
) {
}
