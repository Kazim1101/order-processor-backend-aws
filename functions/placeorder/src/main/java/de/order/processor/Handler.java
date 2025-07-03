package de.order.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.order.processor.entity.PlaceOrder;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.GoneException;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Handler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Region REGION = Region.EU_CENTRAL_1;
    private static final String KITCHEN_CONNECTION_URL = System.getenv("KITCHEN_CONNECTION_URL");
    private static final String BAR_CONNECTION_URL = System.getenv("BAR_CONNECTION_URL");
    private static final String ORDERS_BUCKET = System.getenv("ORDERS_BUCKET");
    private static final String TABLE_NAME = System.getenv("ORDER_TRACKING_TABLE_NAME");

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

    private final S3Client s3Client = S3Client.builder().region(REGION).build();
    private final DynamoDbClient dynamoDb = DynamoDbClient.builder().region(REGION).build();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            log.info("Received event: {}", event);

            String table = getQueryParam(event, "table");
            String branch = getQueryParam(event, "branch");
            String timestamp = FORMATTER.format(Instant.now());

            PlaceOrder.OrderBody orderBody = MAPPER.readValue((String) event.get("body"), PlaceOrder.OrderBody.class);
            List<PlaceOrder.Item> allItems = orderBody.getItems();

            List<PlaceOrder.Item> kitchenItems = filterItems(allItems, false);
            List<PlaceOrder.Item> barItems = filterItems(allItems, true);

            String orderId = generateOrderId();

            PlaceOrder kitchenOrder = createOrderPayload(orderId, table, orderBody, kitchenItems);
            PlaceOrder barOrder = createOrderPayload(orderId, table, orderBody, barItems);

            String orderPath = String.join("/", timestamp.substring(0, 4), timestamp.substring(4, 6), timestamp.substring(6, 8), orderId);
            storeOrderInS3(orderId, branch, table, orderBody, timestamp, orderPath);
            createDynamoDbEntry(orderId, table, timestamp, orderBody.getOrderMetaData().getTotalPrice(), orderPath);

            if (!kitchenItems.isEmpty()) {
                updateOrderStatus(orderId, "kitchen", "pending");
                sendWebSocketNotification(KITCHEN_CONNECTION_URL, "connection_ids/curr_kitchen_item_connection_id.txt", kitchenOrder);
            }

            if (!barItems.isEmpty()) {
                updateOrderStatus(orderId, "bar", "pending");
                sendWebSocketNotification(BAR_CONNECTION_URL, "connection_ids/curr_bar_item_connection_id.txt", barOrder);
            }

            return successResponse("Order Created");

        } catch (Exception e) {
            log.error("Failed to process request", e);
            return errorResponse(e.getMessage());
        }
    }

    // --- Core Functional Units ---

    private String getQueryParam(Map<String, Object> event, String key) {
        Map<String, List<String>> params = (Map<String, List<String>>) event.get("multiValueQueryStringParameters");
        return params.getOrDefault(key, List.of("")).get(0);
    }

    private List<PlaceOrder.Item> filterItems(List<PlaceOrder.Item> items, boolean isBar) {
        return items.stream()
                .filter(item -> isBar == isBarCategory(item.getCategory()))
                .collect(Collectors.toList());
    }

    private boolean isBarCategory(String category) {
        return Set.of(
                "SOFT DRINKS", "Säfte / Juices", "Heiße Getränke / Hot Drinks", "BIER / BEER",
                "LONG DRINKS", "WEISSWEINE / WHITE WINES", "ROT WEINE / RED WINES", "ROSEWEIN / ROSE WINES",
                "WEINE AUS ITALIEN / WINE FROM ITALY", "ROTWEINE AUS ITALY / RED WINES FROM ITALY"
        ).contains(category.toUpperCase(Locale.ROOT));
    }

    private String generateOrderId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private PlaceOrder createOrderPayload(String orderId, String table, PlaceOrder.OrderBody originalBody, List<PlaceOrder.Item> items) {
        PlaceOrder.OrderBody newBody = new PlaceOrder.OrderBody();
        newBody.setItems(items);
        newBody.setOrderMetaData(originalBody.getOrderMetaData());

        PlaceOrder order = new PlaceOrder();
        order.setTable(table);
        order.setBody(newBody);
        order.setOrderId(orderId);
        return order;
    }

    private void storeOrderInS3(String orderId, String branch, String table, PlaceOrder.OrderBody orderBody, String timestamp, String path) throws Exception {
        Map<String, Object> payload = Map.of(
                "orderId", orderId,
                "branch", branch,
                "table", table,
                "body", orderBody,
                "timestamp", timestamp
        );

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(ORDERS_BUCKET)
                        .key(path)
                        .build(),
                RequestBody.fromString(MAPPER.writeValueAsString(payload))
        );

        log.info("Stored order in S3 at {}", path);
    }

    private void createDynamoDbEntry(String orderId, String table, String timestamp, String price, String orderPath) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("orderId", AttributeValue.builder().s(orderId).build());
        item.put("table", AttributeValue.builder().s(table).build());
        item.put("timestamp", AttributeValue.builder().s(timestamp).build());
        item.put("price", AttributeValue.builder().s(price).build());
        item.put("service", AttributeValue.builder().s("pending").build());
        item.put("key", AttributeValue.builder().s(orderPath).build());

        dynamoDb.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

        log.info("Created DynamoDB entry for orderId: {}", orderId);
    }

    private void updateOrderStatus(String orderId, String target, String status) {
        Map<String, AttributeValue> key = Map.of("orderId", AttributeValue.builder().s(orderId).build());
        Map<String, AttributeValue> values = Map.of(":status", AttributeValue.builder().s(status).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET " + target + " = :status")
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
        log.info("Updated {} order status for orderId: {}", target, orderId);
    }

    private void sendWebSocketNotification(String url, String connectionFilePath, PlaceOrder order) {
        try {
            String connectionId = getConnectionIdFromS3(connectionFilePath);
            String payload = MAPPER.writeValueAsString(order);

            try (ApiGatewayManagementApiClient client = buildApiClient(url)) {
                client.postToConnection(PostToConnectionRequest.builder()
                        .connectionId(connectionId)
                        .data(SdkBytes.fromByteBuffer(ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8))))
                        .build());
                log.info("Sent WebSocket message to {} for orderId: {}", connectionId, order.getOrderId());
            }

        } catch (GoneException e) {
            log.warn("WebSocket connection gone for orderId: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification: {}", e.getMessage(), e);
        }
    }

    private ApiGatewayManagementApiClient buildApiClient(String url) {
        return ApiGatewayManagementApiClient.builder()
                .endpointOverride(URI.create(url))
                .region(REGION)
                .build();
    }

    private String getConnectionIdFromS3(String key) {
        try (InputStream inputStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(ORDERS_BUCKET)
                .key(key)
                .build());
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.readLine().trim();
        } catch (Exception e) {
            log.error("Failed to read connection ID from S3: {}", e.getMessage());
            throw new RuntimeException("Connection ID fetch failed", e);
        }
    }

    private Map<String, Object> successResponse(String message) {
        return Map.of(
                "statusCode", 200,
                "body", "\"" + message + "\"",
                "headers", Map.of("Content-Type", "application/json")
        );
    }

    private Map<String, Object> errorResponse(String message) {
        return Map.of(
                "statusCode", 500,
                "body", "\"Error: " + message + "\"",
                "headers", Map.of("Content-Type", "application/json")
        );
    }
}
