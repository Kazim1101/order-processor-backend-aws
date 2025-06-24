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
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
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
    private static final String ORDERS_BUCKET = System.getenv("ORDERS_BUCKET");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);
    private static final String TABLE_NAME = System.getenv("ORDER_TRACKING_TABLE_NAME");

    private final S3Client s3Client = S3Client.builder().region(Region.EU_CENTRAL_1).build();
    private final DynamoDbClient dynamoDb = DynamoDbClient.builder().region(Region.EU_CENTRAL_1).build();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            log.info("Received event: {}", event);

            String table = getQueryParam(event, "table");
            String branch = getQueryParam(event, "branch");

            String timestamp = FORMATTER.format(Instant.now());

            PlaceOrder.OrderBody orderBody = MAPPER.readValue((String) event.get("body"), PlaceOrder.OrderBody.class);
            List<PlaceOrder.Item> allItems = orderBody.getItems();

            List<PlaceOrder.Item> kitchenItems = filterKitchenItems(allItems);
            List<PlaceOrder.Item> barItems = filterBarItems(allItems);

            String orderId = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            PlaceOrder kitchenOrder = createOrderPayload(orderId, table, orderBody, kitchenItems);
            PlaceOrder barOrder = createOrderPayload(orderId, table, orderBody, barItems);

            log.info("Kitchen Order: {}", kitchenOrder);
            log.info("Bar Order: {}", barOrder);

            storeOrderInS3(orderId, branch, table, orderBody, timestamp);
            createDynamoDbEntry(orderId, table, timestamp);

            if (!kitchenItems.isEmpty()) {
                updateKitchenOrder(orderId);
                String connectionId = getConnectionIdFromS3("connection_ids/curr_kitchen_item_connection_id.txt");
                sendToKitchenWebSocket(connectionId, MAPPER.writeValueAsString(kitchenOrder));
            }

            return successResponse("Order Created");

        } catch (Exception e) {
            log.error("Failed to process request: {}", e.getMessage(), e);
            return errorResponse(e.getMessage());
        }
    }

    private void updateKitchenOrder(String orderId) {
        Map<String, AttributeValue> key = Map.of("orderId", AttributeValue.builder().s(orderId).build()
        );

        Map<String, AttributeValue> values = Map.of(
                ":k", AttributeValue.builder().s("pending").build()
        );

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET kitchen = :k")
                .expressionAttributeValues(values)
                .build();
        dynamoDb.updateItem(request);
        log.info("Updated kitchen order status for orderId: {}", orderId);
    }

    private void createDynamoDbEntry(String orderId, String table, String timestamp) {

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("orderId", AttributeValue.builder().s(orderId).build());
        item.put("table", AttributeValue.builder().s(table).build());
        item.put("timestamp", AttributeValue.builder().s(timestamp).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName("OrderTracking")
                .item(item)
                .build();

        dynamoDb.putItem(request);
        log.info("Created DynamoDB entry for orderId: {}, table: {}, timestamp: {}", orderId, table, timestamp);
    }


    private String getQueryParam(Map<String, Object> event, String key) {
        Map<String, List<String>> params = (Map<String, List<String>>) event.get("multiValueQueryStringParameters");
        return params.getOrDefault(key, List.of("")).get(0);
    }

    private List<PlaceOrder.Item> filterKitchenItems(List<PlaceOrder.Item> items) {
        return items.stream()
                .filter(item -> !isHotDrink(item.getCategory()))
                .collect(Collectors.toList());
    }

    private List<PlaceOrder.Item> filterBarItems(List<PlaceOrder.Item> items) {
        return items.stream()
                .filter(item -> isHotDrink(item.getCategory()))
                .collect(Collectors.toList());
    }

    private boolean isHotDrink(String category) {
        return "Hot Drinks".equalsIgnoreCase(category) || "Warm Drinks".equalsIgnoreCase(category);
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

    private void storeOrderInS3(String orderId, String branch, String table, PlaceOrder.OrderBody orderBody, String timestamp) throws Exception {
        String path = String.join("/", timestamp.substring(0, 4), timestamp.substring(4, 6), timestamp.substring(6, 8), orderId);

        Map<String, Object> payload = Map.of(
                "orderId", orderId,
                "branch", branch,
                "table", table,
                "body", orderBody,
                "timestamp", timestamp
        );

        String json = MAPPER.writeValueAsString(payload);

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(ORDERS_BUCKET)
                        .key(path)
                        .build(),
                RequestBody.fromString(json));

        log.info("Stored order in S3 at {}", path);
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

    private void sendToKitchenWebSocket(String connectionId, String messageJson) {
        try (ApiGatewayManagementApiClient client = ApiGatewayManagementApiClient.builder()
                .endpointOverride(URI.create(KITCHEN_CONNECTION_URL))
                .region(REGION)
                .build()) {

            client.postToConnection(PostToConnectionRequest.builder()
                    .connectionId(connectionId)
                    .data(SdkBytes.fromByteBuffer(ByteBuffer.wrap(messageJson.getBytes(StandardCharsets.UTF_8))))
                    .build());

            log.info("Sent message to WebSocket connection: {}", connectionId);

        } catch (GoneException e) {
            log.warn("WebSocket connection gone: {}", connectionId);
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