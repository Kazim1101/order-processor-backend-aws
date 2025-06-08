package de.order.processor;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static software.amazon.awssdk.regions.Region.EU_CENTRAL_1;

@Slf4j
public class Handler implements RequestHandler<Map<String,Object>, Map<String,Object>> {
    private final S3Client client = S3Client.builder().region(EU_CENTRAL_1).build();
    private final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Map<String,Object> handleRequest(Map<String,Object> event, Context ctx) {
        try {
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

            client.putObject(PutObjectRequest.builder()
                            .bucket(System.getenv("ORDERS_BUCKET"))
                            .key(path)
                            .build(),
                    RequestBody.fromString(json));

            return Map.of("statusCode", 200, "body", "\"Order Created\"", "headers",
                    Map.of("Content-Type","application/json"));
        } catch (Exception e) {
//            e.printStackTrace((PrintStream) ctx.getLogger());
            return Map.of("statusCode",500,"body","\"Error: "+e.getMessage()+"\"",
                    "headers", Map.of("Content-Type","application/json"));
        }
    }
}
