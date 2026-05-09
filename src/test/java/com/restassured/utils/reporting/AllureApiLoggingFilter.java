package com.restassured.utils.reporting;

import com.ai.context.TriageEvidenceContext;
import com.ai.model.ApiInteraction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restassured.utils.ConfigManager;
import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * REST Assured filter that adds redacted HTTP request and response evidence to Allure reports.
 *
 * <p>The filter is intended to be registered once in the shared request specification so every
 * API client automatically contributes troubleshooting evidence without duplicating logging code.
 * Sensitive credentials, tokens, cookies, and configured login identifiers are masked before the
 * attachments are written.</p>
 */
public class AllureApiLoggingFilter implements Filter {
    private static final String MASK = "[REDACTED]";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "authorization",
            "cookie",
            "set-cookie",
            "accesstoken",
            "refreshtoken",
            "password",
            "username"
    );
    private static final Pattern JSON_SECRET_PATTERN = Pattern.compile(
            "(?i)(\"(?:accessToken|refreshToken|password|username)\"\\s*:\\s*\")([^\"]*)(\")"
    );
    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile(
            "(?i)(Authorization\\s*[:=]\\s*)(Bearer\\s+)?[^\\s,;\\n]+"
    );
    private static final Pattern COOKIE_PATTERN = Pattern.compile("(?i)((?:Cookie|Set-Cookie)\\s*[:=]\\s*)([^\\n]+)");
    private static final Pattern SENSITIVE_QUERY_PATTERN = Pattern.compile(
            "(?i)([?&](?:username|password|accessToken|refreshToken)=)[^&\\s]+"
    );

    /**
     * Executes the API request and attaches sanitized request/response details to the active Allure test.
     *
     * @param requestSpec mutable REST Assured request details for the current call
     * @param responseSpec REST Assured response specification for the current call
     * @param context filter chain context used to continue request execution
     * @return the API response returned by the next filter or REST Assured itself
     */
    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext context
    ) {
        RequestEvidence requestEvidence = captureRequest(requestSpec);
        ResponseEvidence responseEvidence;

        try {
            Response response = context.next(requestSpec, responseSpec);
            responseEvidence = captureResponse(response);
            publishEvidence(requestEvidence, responseEvidence);
            return response;
        } catch (RuntimeException exception) {
            responseEvidence = failedResponse(exception);
            publishEvidence(requestEvidence, responseEvidence);
            throw exception;
        }
    }

    /**
     * Captures and redacts request details before REST Assured sends the HTTP call.
     */
    private static RequestEvidence captureRequest(FilterableRequestSpecification requestSpec) {
        String method = requestSpec.getMethod();
        String uri = redact(requestSpec.getURI());
        String queryParameters = formatMap(requestSpec.getQueryParams());
        String headers = formatMap(requestSpec.getHeaders().asList().stream()
                .collect(TreeMap::new, (map, header) -> map.put(header.getName(), header.getValue()), TreeMap::putAll));
        String body = redact(bodyAsString(requestSpec.getBody()));
        String attachment = buildRequestAttachment(method, uri, queryParameters, headers, body);

        return new RequestEvidence(method, uri, queryParameters, headers, body, attachment);
    }

    /**
     * Captures and redacts response details after REST Assured receives the HTTP response.
     */
    private static ResponseEvidence captureResponse(Response response) {
        int responseStatus = response.getStatusCode();
        long responseTimeMs = response.getTimeIn(TimeUnit.MILLISECONDS);
        String responseHeaders = formatMap(response.getHeaders().asList().stream()
                .collect(TreeMap::new, (map, header) -> map.put(header.getName(), header.getValue()), TreeMap::putAll));
        String responseBody = redact(responseBody(response));
        String responseAttachment = buildResponseAttachment(responseStatus, responseTimeMs, responseHeaders, responseBody);

        return new ResponseEvidence(responseStatus, responseTimeMs, responseHeaders, responseBody, responseAttachment);
    }

    /**
     * Builds synthetic response evidence when the request fails before a server response is available.
     */
    private static ResponseEvidence failedResponse(RuntimeException exception) {
        String responseBody = "Request failed before an HTTP response was received: " + redact(exception.getMessage());
        String responseAttachment = buildResponseAttachment(0, 0, "(none)", responseBody);

        return new ResponseEvidence(0, 0, "(none)", responseBody, responseAttachment);
    }

    /**
     * Publishes the same sanitized evidence to Allure and the AI triage context.
     */
    private static void publishEvidence(RequestEvidence requestEvidence, ResponseEvidence responseEvidence) {
        Allure.addAttachment("HTTP Request", "text/plain", requestEvidence.attachment(), ".txt");
        Allure.addAttachment("HTTP Response", "text/plain", responseEvidence.attachment(), ".txt");
        TriageEvidenceContext.record(new ApiInteraction(
                requestEvidence.method(),
                requestEvidence.uri(),
                requestEvidence.queryParameters(),
                requestEvidence.headers(),
                requestEvidence.body(),
                responseEvidence.status(),
                responseEvidence.timeMs(),
                responseEvidence.headers(),
                responseEvidence.body(),
                requestEvidence.attachment(),
                responseEvidence.attachment()
        ));
    }

    /**
     * Formats the outbound request into a readable text attachment for Allure.
     */
    private static String buildRequestAttachment(
            String method,
            String uri,
            String queryParameters,
            String requestHeaders,
            String requestBody
    ) {
        return """
                %s %s

                Query parameters:
                %s

                Headers:
                %s

                Body:
                %s
                """.formatted(
                method,
                uri,
                queryParameters,
                requestHeaders,
                requestBody
        );
    }

    /**
     * Formats the inbound response into a readable text attachment for Allure.
     */
    private static String buildResponseAttachment(
            int responseStatus,
            long responseTimeMs,
            String responseHeaders,
            String responseBody
    ) {
        return """
                Status: %d
                Response time: %d ms

                Headers:
                %s

                Body:
                %s
                """.formatted(
                responseStatus,
                responseTimeMs,
                responseHeaders,
                responseBody
        );
    }

    /**
     * Converts headers or parameters into key-value text while masking sensitive keys.
     */
    private static String formatMap(Map<String, ?> values) {
        if (values == null || values.isEmpty()) {
            return "(none)";
        }

        StringBuilder builder = new StringBuilder();
        values.forEach((key, value) -> builder
                .append(key)
                .append(": ")
                .append(isSensitiveKey(key) ? MASK : redact(String.valueOf(value)))
                .append(System.lineSeparator()));
        return builder.toString().trim();
    }

    private static boolean isSensitiveKey(String key) {
        return key != null && SENSITIVE_KEYS.contains(key.toLowerCase());
    }

    /**
     * Serializes request bodies so POJO payloads appear as useful JSON instead of object references.
     */
    private static String bodyAsString(Object body) {
        if (body == null) {
            return "(none)";
        }
        if (body instanceof String stringBody) {
            return prettyJsonOrOriginal(stringBody);
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            return String.valueOf(body);
        }
    }

    /**
     * Pretty-prints bodies that REST Assured has already serialized into compact JSON strings.
     */
    private static String prettyJsonOrOriginal(String body) {
        if (body == null || body.isBlank()) {
            return "(none)";
        }
        try {
            Object json = OBJECT_MAPPER.readValue(body, Object.class);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (JsonProcessingException exception) {
            return body;
        }
    }

    /**
     * Reads response content defensively so report generation does not hide the original test result.
     */
    private static String responseBody(Response response) {
        try {
            return response.getBody().asPrettyString();
        } catch (RuntimeException exception) {
            return "Unable to read response body: " + exception.getMessage();
        }
    }

    /**
     * Masks known sensitive values in headers, query strings, JSON bodies, and configured test credentials.
     */
    private static String redact(String value) {
        if (value == null || value.isBlank()) {
            return "(none)";
        }

        String redacted = JSON_SECRET_PATTERN.matcher(value).replaceAll("$1" + MASK + "$3");
        redacted = AUTHORIZATION_PATTERN.matcher(redacted).replaceAll("$1" + MASK);
        redacted = COOKIE_PATTERN.matcher(redacted).replaceAll("$1" + MASK);
        redacted = SENSITIVE_QUERY_PATTERN.matcher(redacted).replaceAll("$1" + MASK);
        redacted = redactLiteral(redacted, ConfigManager.loginUsername());
        redacted = redactLiteral(redacted, ConfigManager.loginPassword());
        return redacted;
    }

    private static String redactLiteral(String value, String sensitiveValue) {
        if (sensitiveValue == null || sensitiveValue.isBlank()) {
            return value;
        }
        return value.replace(sensitiveValue, MASK);
    }

    private record RequestEvidence(
            String method,
            String uri,
            String queryParameters,
            String headers,
            String body,
            String attachment
    ) {
    }

    private record ResponseEvidence(
            int status,
            long timeMs,
            String headers,
            String body,
            String attachment
    ) {
    }
}
