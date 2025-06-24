package de.order.processor.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceOrder {
    private String orderId;
    private OrderBody body;
    private String table;
    private String timestamp;
    private String branch;

    @Data
    public static class OrderBody {
        private List<Item> items;
        private OrderMetaData orderMetaData;
    }

    @Data
    public static class Item {
        private String name;
        private String category;
        private String id;
        private String price;
        private String description;
        private String quantity;
    }

    @Data
    public static class OrderMetaData {
        private String totalPrice;
        private String timestamp;
        private String paymentMethod;
    }

}


