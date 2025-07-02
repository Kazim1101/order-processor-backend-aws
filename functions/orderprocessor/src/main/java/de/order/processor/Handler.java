package de.order.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.order.processor.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class Handler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        logEvent(event);

        Map<String, String> queryParams = extractQueryParams(event);
        String table = queryParams.getOrDefault("table", "unknown");
        String branch = queryParams.getOrDefault("branch", "unknown");

        logOrderInfo(table, branch, context);

        Map<String, Object> body = buildResponseBody();
        return buildApiResponse(200, body);
    }

    private void logEvent(Map<String, Object> event) {
        log.info("Received event: {}", event);
    }

    private Map<String, String> extractQueryParams(Map<String, Object> event) {
        return Optional.ofNullable((Map<String, String>) event.get("queryStringParameters"))
                .orElseGet(HashMap::new);
    }

    private void logOrderInfo(String table, String branch, Context context) {
        String message = String.format("Received order from table: %s, branch: %s", table, branch);
        log.info(message);
        context.getLogger().log(message);
    }

    private Map<String, Object> buildResponseBody() {
        return Map.of("menu", Util.getMenu());
    }

    private Map<String, Object> buildApiResponse(int statusCode, Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("headers", getDefaultHeaders());
        response.put("body", toJson(body));
        return response;
    }

    private Map<String, String> getDefaultHeaders() {
        return Map.of(
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "*",
                "Content-Type", "application/json"
        );
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}
