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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Handler implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private static final Region REGION = Region.EU_CENTRAL_1;
    private static final String OBJECT_KEY = "connection_ids/curr_kitchen_item_connection_id.txt";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final S3Client s3Client = S3Client.create();
    private final DynamoDbClient dynamoDb = DynamoDbClient.builder().region(REGION).build();

    private static final String TABLE_NAME = System.getenv("ORDER_TRACKING_TABLE_NAME");
    private final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private static final String KITCHEN_CONNECTION_URL = System.getenv("KITCHEN_CONNECTION_URL");

    @Override
    public Object handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        try {
            String eventType = event.getRequestContext().getEventType();
            String connectionId = event.getRequestContext().getConnectionId();

            log.info("Received WebSocket event: {}", eventType);
            switch (eventType.toUpperCase()) {
                case "CONNECT":
                    handleConnect(connectionId);
                    break;
                case "DISCONNECT":
                    handleDisconnect();
                    break;
                case "MESSAGE":
                    handleMessage(event.getBody(), connectionId);
                    break;
                default:
                    log.warn("Unhandled eventType: {}", eventType);
                    break;
            }

        } catch (Exception e) {
            log.error("Error processing WebSocket event ", e);
            return Map.of("statusCode", 500);
        }
        log.info("Kitchen webSocket event processed successfully");
        return Map.of("statusCode", 200);
    }

    private void handleConnect(String connectionId) {
        if (connectionId == null || connectionId.isEmpty()) {
            log.warn("Missing connectionId for CONNECT event");
            return;
        }
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(OBJECT_KEY)
                .build();
        s3Client.putObject(putReq, RequestBody.fromString(connectionId));
        log.info("Stored connectionId: {}", connectionId);
    }

    private void handleDisconnect() {
        DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(OBJECT_KEY)
                .build();
        s3Client.deleteObject(delReq);
        log.info("Deleted connectionId from S3");
    }

    private void handleMessage(String body, String connectionId) throws Exception {
        KitchenOrdersEvent event = MAPPER.readValue(body, KitchenOrdersEvent.class);
        if ("loadKitchenOrders".equalsIgnoreCase(event.getAction()) ||
            "resyncKitchenOrders".equalsIgnoreCase(event.getAction())){
            loadPendingOrders(connectionId);
        } else {
            OrderDone order = MAPPER.readValue(body, OrderDone.class);
            updateKitchenOrder(order.getPayload().getOrderId());
        }
    }

    private void loadPendingOrders(String connectionId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("kitchenIndex")
                .keyConditionExpression("kitchen = :status")
                .expressionAttributeValues(Map.of(":status", AttributeValue.builder().s("pending").build()))
                .build();

        QueryResponse response = dynamoDb.query(request);
        log.info("Loading pending orders from DynamoDB, found {} items", response.items().size());

        for (Map<String, AttributeValue> item : response.items()) {
            String key = item.get("key").s();
            try (ResponseInputStream<?> s3Object = s3Client.getObject(GetObjectRequest.builder().bucket(BUCKET_NAME).key(key).build())) {
                PlaceOrder fullOrder = MAPPER.readValue(s3Object, PlaceOrder.class);
                List<PlaceOrder.Item> kitchenItems = filterKitchenItems(fullOrder.getBody().getItems());
                if (!kitchenItems.isEmpty()) {
                    log.info("Processing orderId: {}, table: {}, items: {}", fullOrder.getOrderId(), fullOrder.getTable(), kitchenItems.size());
                    PlaceOrder kitchenOrder = createOrderPayload(fullOrder.getOrderId(), fullOrder.getTable(), fullOrder.getBody(), kitchenItems);
                    Thread.sleep(1000); // simulate delay
                    sendToWebSocket(connectionId, MAPPER.writeValueAsString(kitchenOrder));
                }
            } catch (Exception e) {
                log.error("Failed to load order from S3 for key: {}", key, e);
            }
        }
    }

    private void sendToWebSocket(String connectionId, String message) {
        try (ApiGatewayManagementApiClient client = ApiGatewayManagementApiClient.builder()
                .endpointOverride(URI.create(KITCHEN_CONNECTION_URL))
                .region(REGION)
                .build()) {
            client.postToConnection(PostToConnectionRequest.builder()
                    .connectionId(connectionId)
                    .data(SdkBytes.fromByteBuffer(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8))))
                    .build());
            log.info("Sent message to connectionId: {}", connectionId);
        } catch (GoneException e) {
            log.warn("Connection gone for id: {}", connectionId);
        }
    }

    private void updateKitchenOrder(String orderId) {
        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("orderId", AttributeValue.builder().s(orderId).build()))
                .updateExpression("SET kitchen = :k")
                .expressionAttributeValues(Map.of(":k", AttributeValue.builder().s("done").build()))
                .build();
        dynamoDb.updateItem(updateRequest);
        log.info("Updated order {} to kitchen=done", orderId);
    }

    private List<PlaceOrder.Item> filterKitchenItems(List<PlaceOrder.Item> items) {
        return items.stream().filter(item -> !isBarItem(item.getCategory())).collect(Collectors.toList());
    }

    private boolean isBarItem(String category) {
        return List.of("SOFT DRINKS", "SÄFTE / JUICES", "HEIßE GETRÄNKE / HOT DRINKS", "BIER / BEER",
                        "LONG DRINKS", "WEISSWEINE / WHITE WINES", "ROT WEINE / RED WINES", "ROSEWEIN / ROSE WINES",
                        "WEINE AUS ITALIEN / WEIN FROM ITALY", "ROTWEINE AUS ITALY / RED WINES FROM ITALY")
                .stream().anyMatch(c -> c.equalsIgnoreCase(category));
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