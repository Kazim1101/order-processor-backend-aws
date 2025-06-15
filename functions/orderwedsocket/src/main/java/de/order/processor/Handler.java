package de.order.processor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayV2WebSocketEvent, Object> {

    private static final String OBJECT_KEY = "connection_ids/curr_kitchen_item_connection_id.txt";
    private final S3Client s3Client = S3Client.create();
    private final String BUCKET_NAME = System.getenv("BUCKET_NAME");

    @Override
    public Object handleRequest(APIGatewayV2WebSocketEvent event, Context context) {
        try {
            // Log the incoming event
            System.out.println("Received WebSocket event: " + event);

            String eventType = event.getRequestContext().getEventType();  // CONNECT, DISCONNECT, MESSAGE
            String connectionId = event.getRequestContext().getConnectionId();
            String domain = event.getRequestContext().getDomainName();
            String stage = event.getRequestContext().getStage();

            System.out.printf("Processing eventType: %s for connection: %s%n", eventType, connectionId);
            System.out.printf("WebSocket domain: %s, stage: %s%n", domain, stage);

            // Build the full endpoint: https://<domain>/<stage>
            URI endpoint = URI.create("https://" + domain + "/" + stage);
            System.out.printf("WebSocket endpoint: %s%n", endpoint);
//            String endpointUrl = "https://ly6u5pu5o6.execute-api.eu-central-1.amazonaws.com/kzm";


            if ("CONNECT".equalsIgnoreCase(eventType)) {
                System.out.printf("In CONNECT event for connection: %s%n", connectionId);
                if (connectionId == null || connectionId.isEmpty()) {
                    return "Missing connectionId for add action";
                }
                System.out.printf("Storing connectionId: %s%n", connectionId);
                System.out.printf("Bucket Name: %s, Object Key: %s%n", BUCKET_NAME, OBJECT_KEY);

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
            }
//            else if ("MESSAGE".equalsIgnoreCase(eventType)) {
//                System.out.printf("In MESSAGE event for connection: %s%n", connectionId);
//                ApiGatewayManagementApiClient client = ApiGatewayManagementApiClient.builder()
//                        .endpointOverride(URI.create(endpointUrl))
//                        .region(Region.EU_CENTRAL_1)
//                        .build();
//                String message = event.getBody();
//
//                System.out.println("Received message: " + message);
//
//                PostToConnectionRequest postRequest = PostToConnectionRequest.builder()
//                        .connectionId(connectionId)
//                        .data(SdkBytes.fromByteBuffer(ByteBuffer.wrap(("Order received: " + message).getBytes())))
//                        .build();
//
//                try {
//                    client.postToConnection(postRequest);
//                } catch (GoneException e) {
//                    System.out.println("Connection gone: " + connectionId);
//                }
//            }
            else {
                System.out.println("Unhandled eventType: " + eventType);
            }

        } catch (Exception e) {
            System.err.println("Error processing WebSocket event: " + e.getMessage());
            e.printStackTrace();
            return Map.of("statusCode", 500);
        }

        return Map.of("statusCode", 200);
    }
}
