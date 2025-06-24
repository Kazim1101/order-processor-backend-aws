package de.order.processor.infrastructure.stack;

import de.order.processor.infrastructure.security.SecurityStack;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

import static de.order.processor.infrastructure.configuration.Configuration.STAGE;

public class OrderProcessorStack extends Stack {

    public OrderProcessorStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public OrderProcessorStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);


        var postProcessor = new OrderMenuFunction(this, "order-processor-" + STAGE, OrderMenuFunction.PostProcessorFunctionProps.builder()
            .build());

        var placeOrder = new PlaceOrderFunction(this, "place-order-" + STAGE, PlaceOrderFunction.PlaceOrderFunctionProps.builder()
            .build());

        var orderWebSocket = new OrderWebSocket(this, "order-websocket-" + STAGE, OrderWebSocket.OrderWebSocketProps.builder()
            .orderProcessorfunction(postProcessor.getFunction())
            .placeOrderFunction(placeOrder.getFunction())
            .ordersBucket(placeOrder.getOrdersBucket())
            .build());

    }

}
