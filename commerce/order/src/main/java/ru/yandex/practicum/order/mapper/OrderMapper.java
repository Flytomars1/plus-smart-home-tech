package ru.yandex.practicum.order.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.order.model.Order;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        if (order == null) return null;

        return OrderDto.builder()
                .orderId(order.getOrderId())
                .shoppingCartId(order.getShoppingCartId())
                .products(order.getProducts())
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .build();
    }

    public Order toEntity(OrderDto orderDto) {
        if (orderDto == null) return null;

        return Order.builder()
                .orderId(orderDto.getOrderId())
                .shoppingCartId(orderDto.getShoppingCartId())
                .products(orderDto.getProducts())
                .paymentId(orderDto.getPaymentId())
                .deliveryId(orderDto.getDeliveryId())
                .state(orderDto.getState())
                .deliveryWeight(orderDto.getDeliveryWeight())
                .deliveryVolume(orderDto.getDeliveryVolume())
                .fragile(orderDto.getFragile())
                .totalPrice(orderDto.getTotalPrice())
                .deliveryPrice(orderDto.getDeliveryPrice())
                .productPrice(orderDto.getProductPrice())
                .build();
    }
}