package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.payment.service.PaymentService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/productCost")
    @ResponseStatus(HttpStatus.OK)
    public Double productCost(@RequestBody OrderDto order) {
        log.info("POST /api/v1/payment/productCost - orderId: {}", order.getOrderId());
        return paymentService.productCost(order);
    }

    @PostMapping("/totalCost")
    @ResponseStatus(HttpStatus.OK)
    public Double getTotalCost(@RequestBody OrderDto order) {
        log.info("POST /api/v1/payment/totalCost - orderId: {}", order.getOrderId());
        return paymentService.getTotalCost(order);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public PaymentDto payment(@RequestBody OrderDto order) {
        log.info("POST /api/v1/payment - orderId: {}", order.getOrderId());
        return paymentService.payment(order);
    }

    @PostMapping("/refund")
    @ResponseStatus(HttpStatus.OK)
    public void paymentSuccess(@RequestBody UUID paymentId) {
        log.info("POST /api/v1/payment/refund - paymentId: {}", paymentId);
        paymentService.paymentSuccess(paymentId);
    }

    @PostMapping("/failed")
    @ResponseStatus(HttpStatus.OK)
    public void paymentFailed(@RequestBody UUID paymentId) {
        log.info("POST /api/v1/payment/failed - paymentId: {}", paymentId);
        paymentService.paymentFailed(paymentId);
    }
}