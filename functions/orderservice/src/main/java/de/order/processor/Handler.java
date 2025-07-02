package de.order.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.order.processor.entity.PlaceOrder;
import de.order.processor.model.Order;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Handler implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private static final Region REGION = Region.EU_CENTRAL_1;
    private static final String TABLE_NAME = System.getenv("ORDER_TRACKING_TABLE_NAME");
    private static final String BUCKET_NAME = System.getenv("ORDERS_BUCKET");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final S3Client s3Client = S3Client.create();
    private final DynamoDbClient dynamoDb = DynamoDbClient.builder().region(REGION).build();

    @Override
    public Object handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        try {
            String httpMethod = event.getHttpMethod();
            Map<String, String> queryParams = event.getQueryStringParameters();

            switch (httpMethod.toUpperCase()) {
                case "PUT":
                    return handlePutRequest(queryParams);
                case "GET":
                    return handleGetRequest();
                default:
                    return buildResponse(200, "Success");
            }

        } catch (Exception e) {
            log.error("Error processing API Gateway event: {}", e.getMessage(), e);
            return buildResponse(500, "Internal Server Error");
        }
    }

    private Map<String, Object> handlePutRequest(Map<String, String> queryParams) {
        String orderNumber = Optional.ofNullable(queryParams)
                .map(params -> params.get("ordernumber"))
                .orElse("");

        if (orderNumber.isBlank()) {
            return buildResponse(400, "Missing or invalid 'ordernumber' query parameter");
        }

        log.info("Received PUT request for order: {}", orderNumber);
        updateOrderServiceStatusToDone(orderNumber);
        return buildResponse(200, "Order " + orderNumber + " marked as done");
    }

    private Map<String, Object> handleGetRequest() throws Exception {
        log.info("Received GET request");
        List<Order> pendingOrders = getPendingServiceOrders();
        String jsonBody = MAPPER.writeValueAsString(pendingOrders);
        return buildResponse(200, jsonBody);
    }

    private List<Order> getPendingServiceOrders() {
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("serviceIndex")
                .keyConditionExpression("service = :pending")
                .expressionAttributeValues(Map.of(
                        ":pending", AttributeValue.builder().s("pending").build()
                ))
                .build();

        QueryResponse response = dynamoDb.query(queryRequest);
        return response.items().stream()
                .map(this::mapItemToOrder)
                .collect(Collectors.toList());
    }

    private Order mapItemToOrder(Map<String, AttributeValue> item) {
        String orderId = item.get("orderId").s();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setBar(getAttr(item, "bar"));
        order.setKitchen(getAttr(item, "kitchen"));
        order.setPrice(getAttr(item, "price"));
        order.setService(item.get("service").s());
        order.setTable(getAttr(item, "table"));
        order.setTimestamp(getAttr(item, "timestamp"));

        Map.Entry<String, List<String>> orderData = getOrderItems(orderId);
        order.setPaymentMethod(orderData.getKey());
        order.setItems(orderData.getValue());

        return order;
    }

    private String getAttr(Map<String, AttributeValue> item, String key) {
        return item.getOrDefault(key, AttributeValue.builder().s("").build()).s();
    }

    private void updateOrderServiceStatusToDone(String orderNumber) {
        dynamoDb.updateItem(UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("orderId", AttributeValue.builder().s(orderNumber).build()))
                .updateExpression("SET service = :done")
                .expressionAttributeValues(Map.of(
                        ":done", AttributeValue.builder().s("done").build()
                ))
                .build());
        log.info("Order {} updated to service='done'", orderNumber);
    }

    private Map.Entry<String, List<String>> getOrderItems(String orderId) {
        String timestamp = FORMATTER.format(Instant.now());
        String key = String.join("/",
                timestamp.substring(0, 4),
                timestamp.substring(4, 6),
                timestamp.substring(6, 8),
                orderId
        );

        try (ResponseInputStream<?> s3Object = s3Client.getObject(GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build())) {

            PlaceOrder placeOrder = MAPPER.readValue(s3Object, PlaceOrder.class);
            List<String> items = placeOrder.getBody().getItems().stream()
                    .map(PlaceOrder.Item::getName)
                    .collect(Collectors.toList());
            return Map.entry(placeOrder.getBody().getOrderMetaData().getPaymentMethod(), items);

        } catch (NoSuchKeyException e) {
            log.warn("Key not found: {}", key);
        } catch (Exception e) {
            log.error("Error reading order items for key: {}", key, e);
        }
        return Map.entry("", Collections.emptyList());
    }

    private Map<String, Object> buildResponse(int statusCode, String body) {
        return Map.of(
                "statusCode", statusCode,
                "headers", getCorsHeaders(),
                "body", body
        );
    }

    private Map<String, String> getCorsHeaders() {
        return Map.of(
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Methods", "*",
                "Access-Control-Allow-Headers", "*",
                "Content-Type", "application/json"
        );
    }
}