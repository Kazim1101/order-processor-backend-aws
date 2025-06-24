package de.order.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.order.processor.entity.KitchenOrdersEvent;
import de.order.processor.entity.OrderDone;
import de.order.processor.entity.PlaceOrder;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.GoneException;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Handler implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private static final Region REGION = Region.EU_CENTRAL_1;
    private static final String OBJECT_KEY = "connection_ids/curr_kitchen_item_connection_id.txt";
    private final S3Client s3Client = S3Client.create();
    private final DynamoDbClient dynamoDb = DynamoDbClient.builder().region(REGION).build();
    private static final String TABLE_NAME = System.getenv("ORDER_TRACKING_TABLE_NAME");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);
    private static final String KITCHEN_CONNECTION_URL = System.getenv("KITCHEN_CONNECTION_URL");


    @Override
    public Object handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        try {
            // Log the incoming event
            System.out.println("Received WebSocket event: " + event);

            String eventType = event.getRequestContext().getEventType();  // CONNECT, DISCONNECT, MESSAGE
            String connectionId = event.getRequestContext().getConnectionId();
            String domain = event.getRequestContext().getDomainName();
            String stage = event.getRequestContext().getStage();

//            System.out.printf("Processing eventType: %s for connection: %s%n", eventType, connectionId);
//            System.out.printf("WebSocket domain: %s, stage: %s%n", domain, stage);

            // Build the full endpoint: https://<domain>/<stage>
            URI endpoint = URI.create("https://" + domain + "/" + stage);
//            System.out.printf("WebSocket endpoint: %s%n", endpoint);
//            String endpointUrl = "https://ly6u5pu5o6.execute-api.eu-central-1.amazonaws.com/kzm";


            if ("CONNECT".equalsIgnoreCase(eventType)) {
                // TODO: create s3 event to load pending orders
                System.out.printf("In CONNECT event for connection: %s%n", connectionId);
                if (connectionId == null || connectionId.isEmpty()) {
                    return "Missing connectionId for add action";
                }
//                System.out.printf("Storing connectionId: %s%n", connectionId);
//                System.out.printf("Bucket Name: %s, Object Key: %s%n", BUCKET_NAME, OBJECT_KEY);

                PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(OBJECT_KEY)
                        .build();

                s3Client.putObject(putReq,
                        RequestBody.fromString(connectionId));

                System.out.println("ClientID stored: " + connectionId);

            } else if ("DISCONNECT".equalsIgnoreCase(eventType)) {
                System.out.printf("In DISCONNECT event for connection: %s%n", connectionId);
                DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(OBJECT_KEY)
                        .build();

                s3Client.deleteObject(delReq);
            } else if ("MESSAGE".equalsIgnoreCase(eventType)) {
                log.info("In MESSAGE event for connection: {}", eventType);
                KitchenOrdersEvent action = MAPPER.readValue(event.getBody(), KitchenOrdersEvent.class);
                log.info("Received KitchenOrdersEvent: " + action);

                if ("loadKitchenOrders".equalsIgnoreCase(action.getAction())) {
                    loadPendingOrdersFromS3(connectionId);
                } else {
                    System.out.printf("In MESSAGE event for connection: %s%n", eventType);
//                loadPendingOrdersFromS3(connectionId);
                    OrderDone orderBody = MAPPER.readValue(event.getBody(), OrderDone.class);
                    updateKitchenOrder(orderBody.getPayload().getOrderId());
                }

            } else {
                System.out.println("Unhandled eventType: " + eventType);
            }

        } catch (Exception e) {
            System.err.println("Error processing WebSocket event: " + e.getMessage());
            e.printStackTrace();
            return Map.of("statusCode", 500);
        }

        return Map.of("statusCode", 200);
    }

    private void loadPendingOrdersFromS3(String connectionId) {

        log.info("Loading pending orders from S3");
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("kitchenIndex") // Use GSI
                .keyConditionExpression("kitchen = :status")
                .expressionAttributeValues(Map.of(
                        ":status", AttributeValue.builder().s("pending").build()
                ))
                .build();
        log.info("Querying DynamoDB with request: {}", queryRequest);
        QueryResponse response = dynamoDb.query(queryRequest);

        String timestamp = FORMATTER.format(Instant.now());
        for (Map<String, AttributeValue> item : response.items()) {
            log.info("Processing item: {}", item);
            String key = String.join("/", timestamp.substring(0, 4), timestamp.substring(4, 6), timestamp.substring(6, 8), item.get("orderId").s());
            log.info("Key: {}", key);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();
            try (ResponseInputStream<?> s3Object = s3Client.getObject(getObjectRequest)) {
                PlaceOrder placeOrder = MAPPER.readValue(s3Object, PlaceOrder.class);

                List<PlaceOrder.Item> allItems = placeOrder.getBody().getItems();
                log.info("All Items: {}", allItems);

                List<PlaceOrder.Item> kitchenItems = filterKitchenItems(allItems);
                log.info("Filtered Kitchen Items: {}", kitchenItems);
                PlaceOrder kitchenOrder = createOrderPayload(placeOrder.getOrderId(), placeOrder.getTable(), placeOrder.getBody(), kitchenItems);
                if (!kitchenItems.isEmpty()) {
                    log.info("Sleep for 1 second before sending to WebSocket");
                    Thread.sleep(1000);
                    log.info("Sending kitchen order to WebSocket: {}", kitchenOrder);
                    sendToKitchenWebSocket(connectionId, MAPPER.writeValueAsString(kitchenOrder));
                }
            } catch (Exception e) {
                log.error("Error reading S3 object for key: " + key, e);
            }
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
            log.warn("WebSocket connection gone: {}", e);
        }
    }

    private void updateKitchenOrder(String orderId) {
        Map<String, AttributeValue> key = Map.of("orderId", AttributeValue.builder().s(orderId).build()
        );

        Map<String, AttributeValue> values = Map.of(
                ":k", AttributeValue.builder().s("done").build()
        );

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET kitchen = :k")
                .expressionAttributeValues(values)
                .build();
        dynamoDb.updateItem(request);
        log.info("Updated Kitchen Order to DONE for OrderID: " + orderId);
    }

    private List<PlaceOrder.Item> filterKitchenItems(List<PlaceOrder.Item> items) {
        return items.stream()
                .filter(item -> !isHotDrink(item.getCategory()))
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
}
