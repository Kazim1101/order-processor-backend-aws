package de.order.processor.infrastructure.stack;

import lombok.Builder;
import lombok.Value;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigatewayv2.alpha.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Code;
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

    private final String webSocketUrl;
    private final String postToConnectionUrl;

    public String getWebSocketUrl() {
        return webSocketUrl;
    }

    public String getPostToConnectionUrl() {
        return postToConnectionUrl;
    }

    public OrderWebSocket(Construct scope, String id, OrderWebSocketProps props) {
        super(scope, id);

        Function orderHandler = Function.Builder.create(this, "OrderWebSocketHandler")
                .runtime(Runtime.JAVA_21)
                .handler("de.order.processor.Handler::handleRequest")
                .code(Code.fromAsset("../functions/orderwedsocket/build/libs/lambda.jar"))
                .role(buildRole(this))
                .timeout(Duration.seconds(60))
                .environment(Map.ofEntries(
                        Map.entry("BUCKET_NAME", props.ordersBucket.getBucketName())
                ))
                .build();

        WebSocketApi webSocketApi = WebSocketApi.Builder.create(this, "OrderWebSocketApi")
                .apiName("order-websocket-api")
                .build();

        WebSocketRouteIntegration connectIntegration = new WebSocketLambdaIntegration("ConnectIntegration", orderHandler);
        WebSocketRouteIntegration disconnectIntegration = new WebSocketLambdaIntegration("DisconnectIntegration", orderHandler);
        WebSocketRouteIntegration defaultIntegration = new WebSocketLambdaIntegration("DefaultIntegration", orderHandler);

        WebSocketRoute.Builder.create(this, "ConnectRoute")
                .webSocketApi(webSocketApi)
                .routeKey("$connect")
                .integration(connectIntegration)
                .build();

        WebSocketRoute.Builder.create(this, "DisconnectRoute")
                .webSocketApi(webSocketApi)
                .routeKey("$disconnect")
                .integration(disconnectIntegration)
                .build();

        WebSocketRoute.Builder.create(this, "DefaultRoute")
                .webSocketApi(webSocketApi)
                .routeKey("$default")
                .integration(defaultIntegration)
                .build();

        WebSocketStage stage = WebSocketStage.Builder.create(this, STAGE)
                .webSocketApi(webSocketApi)
                .stageName(STAGE)
                .autoDeploy(true)
                .build();

        // Construct WebSocket and POST URL
        this.webSocketUrl = "wss://" + webSocketApi.getApiId() + ".execute-api." + REGION + ".amazonaws.com/" + STAGE;
        // POST URL for sending messages to connected clients
        this.postToConnectionUrl = "https://" + webSocketApi.getApiId() + ".execute-api." + REGION + ".amazonaws.com/" + STAGE;

    }

    private Role buildRole(OrderWebSocket scope) {
        return Role.Builder.create(scope, "socketHandlerRole")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromManagedPolicyArn(this, "AWSLambdaBasicExecutionRole", "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonSQSReadOnlyAccess", "arn:aws:iam::aws:policy/AmazonSQSReadOnlyAccess"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonS3FullAccess", "arn:aws:iam::aws:policy/AmazonS3FullAccess"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonAPIGatewayInvokeFullAccess", "arn:aws:iam::aws:policy/AmazonAPIGatewayInvokeFullAccess"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AWSLambdaVPCAccessExecutionRole", "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole")
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


