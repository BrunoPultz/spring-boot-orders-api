package com.brunopultz.orderms.controller.dto;

import java.math.BigDecimal;

import com.brunopultz.orderms.entity.OrderEntity;

public record OrderResponse(Long orderId,
        Long customerId,
        BigDecimal total) {

    public static OrderResponse fromEntity(OrderEntity entity) {
        return new OrderResponse(entity.getOrderId(), entity.getCustomerId(), entity.getTotal());
    }

}
