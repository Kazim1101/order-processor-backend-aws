package de.order.processor.infrastructure.stack;

import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.services.apigateway.*;
import software.constructs.Construct;

import java.util.List;

import static de.order.processor.infrastructure.configuration.Configuration.STAGE;

public class OrderProcessorApi extends Construct {

  private final RestApi restApi;

  public OrderProcessorApi(@NotNull Construct scope, @NotNull String id, ApiProps props) {
    super(scope, id);

      this.restApi = new RestApi(this, "RestApi", RestApiProps.builder()
          .restApiName(props.getApiName())
          .description(props.getDescription())
          .deployOptions(StageOptions.builder()
              .stageName(STAGE)
              .tracingEnabled(true)
              .dataTraceEnabled(true)
              .loggingLevel(MethodLoggingLevel.INFO)
              .throttlingBurstLimit(10)
              .throttlingRateLimit(8)
              .build())
          .defaultCorsPreflightOptions(CorsOptions.builder()
              .allowOrigins(Cors.ALL_ORIGINS)
              .allowMethods(Cors.ALL_METHODS)
              .allowHeaders(List.of(
                  "Content-Type",
                  "X-Amz-Date",
                  "Authorization",
                  "X-Api-Key",
                  "requestid",
                  "sessionid",
                  "X-TraceId",
                  "x-caseid"
              ))
              .allowCredentials(true)
              .build()
          ).build()
      );


      new CfnOutput(this, "ApiUrl" + STAGE.toUpperCase(), CfnOutputProps.builder()
        .value(restApi.getUrl())
        .exportName(props.getApiName() + "Url").build());
  }

  public RestApi getRestApi() {
    return restApi;
  }

  @Builder
  @Value
  public static class ApiProps {
    String apiName;
    String description;
  }
}
