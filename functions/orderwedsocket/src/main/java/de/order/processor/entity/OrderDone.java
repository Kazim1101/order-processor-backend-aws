package de.order.processor.entity;

import lombok.Data;


@Data
public class OrderDone {
    private String action;
    private Payload payload;

    @Data
    public static class Payload {
        private String type;
        private String status;
        private String orderId;
        private String completedAt;
    }
}
