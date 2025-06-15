package de.order.processor;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.GoneException;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static software.amazon.awssdk.regions.Region.EU_CENTRAL_1;

@Slf4j
public class Handler implements RequestHandler<Map<String,Object>, Map<String,Object>> {
    private final S3Client s3_client = S3Client.builder().region(EU_CENTRAL_1).build();
    private final String CONNECTION_URL = System.getenv("CONNECTION_URL");
    private final String ORDERS_BUCKET = System.getenv("ORDERS_BUCKET");
    private final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Map<String,Object> handleRequest(Map<String,Object> event, Context ctx) {
        try {

            System.out.printf("Received event: %s%n", event);
            String table = ((Map<String, java.util.List<String>>) event.get("multiValueQueryStringParameters"))
                    .get("table").get(0);
            String branch = ((Map<String, java.util.List<String>>) event.get("multiValueQueryStringParameters"))
                    .get("branch").get(0);

            String body = (String) event.get("body");
            Map<?,?> payload = MAPPER.readValue(body, Map.class);

            String ts = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .withZone(ZoneOffset.UTC)
                    .format(Instant.now());

            String key = ts + "_" + table + ".json";
            String path = String.join("/", ts.substring(0,4), ts.substring(4,6), ts.substring(6,8), key);

            String json = MAPPER.writeValueAsString(Map.of(
                    "branch", branch,
                    "table", table,
                    "body", payload,
                    "timestamp", ts
            ));
            System.out.printf("Storing order in S3 at path: %s with content: %s%n", path, json);

            s3_client.putObject(PutObjectRequest.builder()
                            .bucket(ORDERS_BUCKET)
                            .key(path)
                            .build(),
                    RequestBody.fromString(json));

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(ORDERS_BUCKET)
                    .key("connection_ids/curr_kitchen_item_connection_id.txt")
                    .build();


            String connectionId = "";
            System.out.printf("Retrieving connection ID from S3 for key: %s%n", key);
            try (InputStream inputStream = s3_client.getObject(getObjectRequest);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                connectionId = content.toString().trim();

            } catch (Exception e) {
                System.err.printf("Error retrieving connection ID from S3: %s%n", e.getMessage());
                throw new RuntimeException("Failed to get object from S3: " + e.getMessage(), e);
            }


            System.out.printf("Retrieved connection ID: %s%n", connectionId);
            ApiGatewayManagementApiClient client = ApiGatewayManagementApiClient.builder()
                    .endpointOverride(URI.create(CONNECTION_URL))
                    .region(Region.EU_CENTRAL_1)
                    .build();

            PostToConnectionRequest postRequest = PostToConnectionRequest.builder()
                    .connectionId(connectionId)
                    .data(SdkBytes.fromByteBuffer(ByteBuffer.wrap((json).getBytes())))
                    .build();
            System.out.printf("Posting to connection: %s%n", connectionId);

            try {
                client.postToConnection(postRequest);
                System.out.printf("Order posted to connection: %s%n", connectionId);
            } catch (GoneException e) {
                System.out.println("Connection gone: " + connectionId);
            }



            return Map.of("statusCode", 200, "body", "\"Order Created\"", "headers",
                    Map.of("Content-Type","application/json"));
        } catch (Exception e) {
            System.out.printf("Error processing request: %s%n", e.getMessage());
//            e.printStackTrace((PrintStream) ctx.getLogger());
            return Map.of("statusCode",500,"body","\"Error: "+e.getMessage()+"\"",
                    "headers", Map.of("Content-Type","application/json"));
        }
    }
}
