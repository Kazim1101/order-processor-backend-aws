package de.order.processor.infrastructure.stack;

import lombok.Builder;
import lombok.Value;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupProps;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

import static de.order.processor.infrastructure.configuration.Configuration.DEFAULT_LOG_RETENTION;
import static de.order.processor.infrastructure.configuration.Configuration.STAGE;
import static software.amazon.awscdk.services.iam.Effect.ALLOW;

public class OrderServiceFunction extends Construct {

    private final Function function;

    public OrderServiceFunction(Construct scope, String id, OrderServiceFunctionProps props) {
        super(scope, id);

        this.function = new Function(this, id, FunctionProps.builder()
                .functionName(id)
                .runtime(Runtime.JAVA_21)
                .memorySize(350)
                .architecture(Architecture.ARM_64)
                .timeout(Duration.seconds(60))
                //Todo: Craete a VPC
//            .vpc(buildVpc())
                .handler("de.order.processor.Handler::handleRequest")
                .code(Code.fromAsset("../functions/orderservice/build/libs/lambda.jar"))
                .tracing(Tracing.ACTIVE)
                .role(buildRole(this))
                .environment(Map.ofEntries(
                        Map.entry("ORDERS_BUCKET", props.ordersBucket.getBucketName()),
                        Map.entry("STAGE", STAGE),
                        Map.entry("ORDER_TRACKING_TABLE_NAME", "OrderTrackingkzm")
                )).build());


        new LogGroup(this, "LogGroup", LogGroupProps.builder()
                .logGroupName("/aws/lambda/" + function.getFunctionName())
                .retention(DEFAULT_LOG_RETENTION)
//            .encryptionKey(props.applicationKey)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build());

        RestApi restApi = RestApi.Builder.create(this, "RestaurantServiceApi-" + STAGE)
                .restApiName("RestaurantServiceApi" + STAGE)
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(Cors.ALL_ORIGINS)
                        .allowMethods(Cors.ALL_METHODS)
                        .build())
                .build();

        Resource v1Resource = restApi.getRoot().addResource("v1");

        Resource servicesResource = v1Resource.addResource("services");
        servicesResource.addMethod("GET",
                new LambdaIntegration(this.function, LambdaIntegrationOptions.builder().proxy(true).build()),
                MethodOptions.builder().authorizationType(AuthorizationType.NONE).build());

        Resource serviceResource = v1Resource.addResource("service");
        serviceResource.addMethod("PUT",
                new LambdaIntegration(this.function, LambdaIntegrationOptions.builder().proxy(true).build()),
                MethodOptions.builder().authorizationType(AuthorizationType.NONE).build());

    }

    @Builder
    @Value
    public static class OrderServiceFunctionProps {
        IBucket ordersBucket;
    }

    private Role buildRole(OrderServiceFunction scope) {
        return Role.Builder.create(scope, "PreSignerRole")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromManagedPolicyArn(this, "AWSLambdaBasicExecutionRole", "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonSQSReadOnlyAccess", "arn:aws:iam::aws:policy/AmazonSQSReadOnlyAccess"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonS3FullAccess", "arn:aws:iam::aws:policy/AmazonS3FullAccess"),
                        ManagedPolicy.fromManagedPolicyArn(this, "SecretsManagerReadWrite", "arn:aws:iam::aws:policy/SecretsManagerReadWrite"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AWSLambdaVPCAccessExecutionRole", "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"),
                        ManagedPolicy.fromManagedPolicyArn(this, "AmazonDynamoDBFullAccess", "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess")
                ))
                .inlinePolicies(Map.of(
                        "allowKMSInvocation", PolicyDocument.Builder.create()
                                .statements(List.of(
                                        PolicyStatement.Builder.create()
                                                .effect(ALLOW)
                                                .actions(List.of("kms:*"))
                                                .resources(List.of("*"))
                                                .build()
                                )).build()
                )).build();
    }
}


