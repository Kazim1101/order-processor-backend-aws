package de.order.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.order.processor.util.Util;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Handler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        // Extract query parameters
        System.out.printf("Received event: %s%n", event);
        Map<String, String> queryParams = (Map<String, String>) event.get("queryStringParameters");
        String table = queryParams != null ? queryParams.getOrDefault("table", "unknown") : "unknown";
        String branch = queryParams != null ? queryParams.getOrDefault("branch", "unknown") : "unknown";

        // Log to CloudWatch
        System.out.printf("Received order from table: %s, branch: %s%n", table, branch);
        context.getLogger().log("Received order from table: " + table + ", branch: " + branch);

        Map<String, Object> responseBody = Map.of("menu", Util.getMenu());

        // Build full API Gateway response
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("headers", Map.of(
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "*",
                "Content-Type", "application/json"
        ));
        response.put("body", toJson(responseBody));

        return response;
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}