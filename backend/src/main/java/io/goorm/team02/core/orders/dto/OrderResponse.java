package io.goorm.team02.core.orders.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderResponse {

    private Long id;
    private String customerName;
    private String phoneNumber;
    private String address;
    private String requestMessage;
    private String orderMemo;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;

    public OrderResponse(Long id, String customerName, String phoneNumber, String address,
            String requestMessage, String orderMemo, BigDecimal totalAmount,
            List<OrderItemResponse> items) {
        this.id = id;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.requestMessage = requestMessage;
        this.orderMemo = orderMemo;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    // getter
    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public String getOrderMemo() {
        return orderMemo;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}
