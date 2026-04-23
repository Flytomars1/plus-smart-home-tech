package ru.yandex.practicum.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;

import ru.yandex.practicum.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<OrderDto> getClientOrders(@RequestParam String username) {
        log.info("GET /api/v1/order?username={}", username);
        return orderService.getClientOrders(username);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request) {
        log.info("PUT /api/v1/order");
        return orderService.createNewOrder(request);
    }

    @PostMapping("/return")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto productReturn(@Valid @RequestBody ProductReturnRequest request) {
        log.info("POST /api/v1/order/return");
        return orderService.productReturn(request);
    }

    @PostMapping("/payment")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto payment(@RequestBody UUID orderId) {
        log.info("POST /api/v1/order/payment - orderId: {}", orderId);
        return orderService.payment(orderId);
    }

    @PostMapping("/payment/failed")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto paymentFailed(@RequestBody UUID orderId) {
        log.info("POST /api/v1/order/payment/failed - orderId: {}", orderId);
        return orderService.paymentFailed(orderId);
    }

    @PostMapping("/delivery")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto delivery(@RequestBody DeliveryRequest request) {
        log.info("POST /api/v1/order/delivery - orderId: {}", request.getDeliveryDto().getOrderId());
        return orderService.delivery(
                request.getDeliveryDto().getOrderId(),
                request.getDeliveryDto().getToAddress()
        );
    }

    @PostMapping("/delivery/failed")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto deliveryFailed(@RequestBody UUID orderId) {
        log.info("POST /api/v1/order/delivery/failed - orderId: {}", orderId);
        return orderService.deliveryFailed(orderId);
    }

    @PostMapping("/completed")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto complete(@RequestBody UUID orderId) {
        log.info("POST /api/v1/order/completed - orderId: {}", orderId);
        return orderService.complete(orderId);
    }

    @PostMapping("/calculate/total")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto calculateTotalCost(@RequestBody UUID orderId) {
        log.info("POST /api/v1/order/calculate/total - orderId: {}", orderId);
        return orderService.calculateTotalCost(orderId);
    }

    @PostMapping("/calculate/delivery")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto calculateDeliveryCost(@RequestBody CalculateDeliveryCostRequest request) {
        log.info("POST /api/v1/order/calculate/delivery - orderId: {}, address: {}",
                request.getOrderId(), request.getDeliveryAddress());
        return orderService.calculateDeliveryCost(request.getOrderId(), request.getDeliveryAddress());
    }

    @PostMapping("/assembly")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto assembly(@RequestBody UUID orderId) {
        log.info("POST /api/v1/order/assembly - orderId: {}", orderId);
        return orderService.assembly(orderId);
    }

    @PostMapping("/assembly/failed")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto assemblyFailed(@RequestBody UUID orderId) {
        log.info("POST /api/v1/order/assembly/failed - orderId: {}", orderId);
        return orderService.assemblyFailed(orderId);
    }
}