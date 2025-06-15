package de.order.processor.infrastructure.stack;

import lombok.Builder;
import lombok.Value;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.kms.IKey;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupProps;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

import static de.order.processor.infrastructure.configuration.Configuration.*;
import static software.amazon.awscdk.services.iam.Effect.ALLOW;

public class PlaceOrderFunction extends Construct {

    private final Function function;
    private final Bucket ordersBucket;

    public PlaceOrderFunction(Construct scope, String id, PlaceOrderFunctionProps props) {
        super(scope, id);

        this.ordersBucket = Bucket.Builder.create(this, "berlin-restaurant-ferris-orders-" + STAGE)
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();

        this.function = new Function(this, id, FunctionProps.builder()
            .functionName(id)
            .runtime(Runtime.JAVA_21)
            .memorySize(512)
            .architecture(Architecture.ARM_64)
            .timeout(Duration.seconds(60))
            //Todo: Craete a VPC
//            .vpc(buildVpc())
            .handler("de.order.processor.Handler::handleRequest")
            .code(Code.fromAsset("../functions/placeorder/build/libs/lambda.jar"))
            .tracing(Tracing.ACTIVE)
            .role(buildRole(this))
            .environment(Map.ofEntries(
                Map.entry("ORDERS_BUCKET", ordersBucket.getBucketName()),
                Map.entry("STAGE", STAGE),
                Map.entry("CONNECTION_URL", "https://ly6u5pu5o6.execute-api.eu-central-1.amazonaws.com/kzm")
            )).build());

        new LogGroup(this, "LogGroup", LogGroupProps.builder()
            .logGroupName("/aws/lambda/" + function.getFunctionName())
            .retention(DEFAULT_LOG_RETENTION)
//            .encryptionKey(props.applicationKey)
            .removalPolicy(RemovalPolicy.DESTROY)
            .build());

        RestApi api = RestApi.Builder.create(this, "OrderApi")
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(Cors.ALL_ORIGINS)
                        .allowMethods(Cors.ALL_METHODS)
                        .build())
                .build();

        api.getRoot().addResource("v1").addResource("order")
                .addMethod("POST", LambdaIntegration.Builder.create(function).proxy(true).build());

        ordersBucket.grantReadWrite(function);

    }


    public IFunction getFunction() {
        return function;
    }

    public IBucket getOrdersBucket() {return ordersBucket;}

    @Builder
    @Value
    public static class PlaceOrderFunctionProps {
//        IKey applicationKey;
    }

    private Role buildRole(PlaceOrderFunction scope) {
        return Role.Builder.create(scope, "PreSignerRole")
            .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
            .managedPolicies(List.of(
                ManagedPolicy.fromManagedPolicyArn(this, "AWSLambdaBasicExecutionRole", "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"),
                ManagedPolicy.fromManagedPolicyArn(this, "AmazonSQSReadOnlyAccess", "arn:aws:iam::aws:policy/AmazonSQSReadOnlyAccess"),
                ManagedPolicy.fromManagedPolicyArn(this, "AmazonS3FullAccess", "arn:aws:iam::aws:policy/AmazonS3FullAccess"), ManagedPolicy.fromManagedPolicyArn(this, "AmazonAPIGatewayInvokeFullAccess", "arn:aws:iam::aws:policy/AmazonAPIGatewayInvokeFullAccess"), ManagedPolicy.fromManagedPolicyArn(this, "SecretsManagerReadWrite", "arn:aws:iam::aws:policy/SecretsManagerReadWrite"),
                ManagedPolicy.fromManagedPolicyArn(this, "AWSLambdaVPCAccessExecutionRole", "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole")
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

    private void addApiMethod(IRestApi restApi, String method, IFunction function, String resourcePath) {
        Resource v1Resource = restApi.getRoot().addResource("v1");
        Resource orderResource = v1Resource.addResource(resourcePath);

        orderResource.addMethod(method,
                new LambdaIntegration(function, LambdaIntegrationOptions.builder().proxy(true).build()),
                MethodOptions.builder()
                        .authorizationType(AuthorizationType.NONE) // change to CUSTOM if you add authorizer
                        .build()
        );
    }

}
