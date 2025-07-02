package de.order.processor.model;

import lombok.Data;

import java.util.List;

@Data
public class Order {
    private String orderId;
    private String bar;
    private String kitchen;
    private String price;
    private String service;
    private String table;
    private String timestamp;
    private List<String> items;
    private String paymentMethod;
}
