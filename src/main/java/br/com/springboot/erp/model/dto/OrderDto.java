package br.com.springboot.erp.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.springboot.erp.model.entity.Order;
import br.com.springboot.erp.model.entity.Status;

public record OrderDto(
        Long id,
        String orderNumber,
        LocalDateTime orderDate,
        Status status,
        BigDecimal totalAmount
) implements Serializable {

    public static OrderDto from(Order order) {
        return new OrderDto(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount()
        );
    }
}