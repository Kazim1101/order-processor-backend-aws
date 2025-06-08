package de.order.processor.infrastructure;

import de.order.processor.infrastructure.stack.OrderProcessorStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Map;
import static de.order.processor.infrastructure.configuration.Configuration.STAGE;


public class OrderProcessorApp {

    public static void main(final String[] args) {
        App app = new App();

        new OrderProcessorStack(app, "OrderProcessorStack", StackProps.builder()
            .stackName("order-processor-backend-aws-" + STAGE)
            .env(ACCOUNT)
            .tags(Map.of(
                "application-id", "order-processor-backend-aws-" + STAGE,
                "owner:domain", "berlin",
                "cost-center", "1111"
            )).build());
        app.synth();
    }

    static Environment ACCOUNT = Environment.builder()
        .account("066964539099")
        .region("eu-central-1")
        .build();

}
