package de.order.processor.infrastructure.stack;

import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigatewayv2.alpha.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;
import software.amazon.awscdk.services.apigatewayv2.integrations.alpha.WebSocketLambdaIntegration;


import java.util.List;
import java.util.Map;

import static de.order.processor.infrastructure.configuration.Configuration.STAGE;
import static software.amazon.awscdk.Aws.ACCOUNT_ID;
import static software.amazon.awscdk.Aws.REGION;
import static software.amazon.awscdk.services.iam.Effect.ALLOW;

public class OrderWebSocket extends Construct {

    public OrderWebSocket(Construct scope, String id, OrderWebSocketProps props) {
        super(scope, id);

        Table table = Table.Builder.create(this, "OrderTrackingTable" + STAGE)
                .tableName("OrderTracking" + STAGE)
                .partitionKey(Attribute.builder()
                        .name("orderId")
                        .type(AttributeType.STRING)
                        .build())
                .build();

        table.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
                .indexName("kitchenIndex")
                .partitionKey(Attribute.builder()
                        .name("kitchen")
                        .type(AttributeType.STRING)
                        .build())
                .projectionType(ProjectionType.ALL)
                .build());

        table.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
                .indexName("barIndex")
                .partitionKey(Attribute.builder()
                        .name("bar")
                        .type(AttributeType.STRING)
                        .build())
                .projectionType(ProjectionType.ALL)
                .build());

        table.addGlobalSecondaryIndex(GlobalSecondaryIndexProps.builder()
                .indexName("serviceIndex")
                .partitionKey(Attribute.builder()
                        .name("service")
                        .type(AttributeType.STRING)
                        .build())
                .projectionType(ProjectionType.ALL)
                .build());

        Function kitchenHandler = Function.Builder.create(this, "OrderWebSocketHandler" + STAGE)
                .runtime(Runtime.JAVA_21)
                .handler("de.order.processor.Handler::handleRequest")
                .code(Code.fromAsset("../functions/orderwedsocket/build/libs/lambda.jar"))
                .role(buildRole(this, "KitchenHandler"))
                .timeout(Duration.seconds(60))
                .tracing(Tracing.DISABLED)
                .environment(Map.ofEntries(
                        Map.entry("BUCKET_NAME", props.ordersBucket.getBucketName()),
                        Map.entry("ORDER_TRACKING_TABLE_NAME", "OrderTracking" + STAGE),
                        Map.entry("KITCHEN_CONNECTION_URL", "https://7vf1ii741a.execute-api.eu-central-1.amazonaws.com/kzm")
                ))
                .build();

        WebSocketApi kitchenWebSocketApi = createWebSocketApi("KitchenWebSocketApi-", kitchenHandler);

        Function barHandler = Function.Builder.create(this, "BarWebSocketHandler" + STAGE)
                .runtime(Runtime.JAVA_21)
                .handler("de.order.processor.Handler::handleRequest")
                .code(Code.fromAsset("../functions/barwebsocket/build/libs/lambda.jar"))
                .role(buildRole(this, "BarHandler"))
                .timeout(Duration.seconds(60))
                .tracing(Tracing.DISABLED)
                .environment(Map.ofEntries(
                        Map.entry("BUCKET_NAME", props.ordersBucket.getBucketName()),
                        Map.entry("ORDER_TRACKING_TABLE_NAME", "OrderTracking" + STAGE),
                        Map.entry("BAR_CONNECTION_URL", "https://ggw3hxpaa1.execute-api.eu-central-1.amazonaws.com/kzm")
                ))
                .build();

        WebSocketApi barWebSocketApi = createWebSocketApi("BarWebSocketApi-", barHandler);

//        Function serviceHandler = Function.Builder.create(this, "OrderServiceHandler" + STAGE)
//                .runtime(Runtime.JAVA_21)
//                .handler("de.order.processor.Handler::handleRequest")
//                .code(Code.fromAsset("../functions/orderservice/build/libs/lambda.jar"))
//                .role(buildRole(this, "ServiceHandler"))
//                .timeout(Duration.seconds(60))
//                .environment(Map.ofEntries(
//                        Map.entry("BUCKET_NAME", props.ordersBucket.getBucketName()),
//                        Map.entry("ORDER_TRACKING_TABLE_NAME", "OrderTracking" + STAGE),
//                        Map.entry("SERVICE_CONNECTION_URL", "https://e5zeq8vj9d.execute-api.eu-central-1.amazonaws.com/kzm")
//                ))
//                .build();
//
//        WebSocketApi OrderServiceApi = createWebSocketApi("OrderServiceApiApi-", serviceHandler);
    }

    private @NotNull WebSocketApi createWebSocketApi(String prefix, Function handler) {
        WebSocketApi webSocketApi = WebSocketApi.Builder.create(this, prefix + "Api" + STAGE)
                .apiName(prefix + STAGE)
                .build();

        WebSocketRouteIntegration connectIntegration = new WebSocketLambdaIntegration(prefix + "ConnectIntegration", handler);
        WebSocketRouteIntegration disconnectIntegration = new WebSocketLambdaIntegration(prefix + "DisconnectIntegration", handler);
        WebSocketRouteIntegration defaultIntegration = new WebSocketLambdaIntegration(prefix + "DefaultIntegration", handler);

        WebSocketRoute.Builder.create(this, prefix + "ConnectRoute" + STAGE)
                .webSocketApi(webSocketApi)
                .routeKey("$connect")
                .integration(connectIntegration)
                .build();

        WebSocketRoute.Builder.create(this, prefix + "DisconnectRoute" + STAGE)
                .webSocketApi(webSocketApi)
                .routeKey("$disconnect")
                .integration(disconnectIntegration)
                .build();

        WebSocketRoute.Builder.create(this, prefix + "DefaultRoute" + STAGE)
                .webSocketApi(webSocketApi)
                .routeKey("$default")
                .integration(defaultIntegration)
                .build();

        WebSocketStage.Builder.create(this, prefix + "Stage" + STAGE)
                .webSocketApi(webSocketApi)
                .stageName(STAGE)
                .autoDeploy(true)
                .build();

        return webSocketApi;
    }

    private Role buildRole(OrderWebSocket scope, String id) {
        return Role.Builder.create(scope, "socketHandlerRole" + id)
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromManagedPolicyArn(this, "AWSLambdaBasicExecutionRole" + id, "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonSQSReadOnlyAccess" + id, "arn:aws:iam::aws:policy/AmazonSQSReadOnlyAccess"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonS3FullAccess" + id, "arn:aws:iam::aws:policy/AmazonS3FullAccess"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonAPIGatewayInvokeFullAccess" + id, "arn:aws:iam::aws:policy/AmazonAPIGatewayInvokeFullAccess"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AWSLambdaVPCAccessExecutionRole" + id, "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonDynamoDBFullAccess" + id, "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess")
                ))
                .inlinePolicies(Map.of(
                        "allowSocketConnectionInvocation", PolicyDocument.Builder.create()
                                .statements(List.of(
                                        PolicyStatement.Builder.create()
                                                .effect(ALLOW)
                                                .actions(List.of("kms:*"))
                                                .resources(List.of("*"))
                                                .build(),
                                        PolicyStatement.Builder.create()
                                                .actions(List.of("execute-api:ManageConnections"))
                                                .resources(List.of("arn:aws:execute-api:" + REGION + ":" + ACCOUNT_ID + ":ly6u5pu5o6/*/POST/@connections/*"))
                                                .build()
                                )).build()
                )).build();
    }

    @Builder
    @Value
    public static class OrderWebSocketProps {
        IFunction orderProcessorfunction;
        IFunction placeOrderFunction;
        IBucket ordersBucket;
//        IKey applicationKey;
    }
}


