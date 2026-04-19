package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

import java.util.UUID;

@RequestMapping("/api/v1/payment")
public interface PaymentApi {

    @PostMapping("/productCost")
    Double productCost(@RequestBody OrderDto order);

    @PostMapping("/totalCost")
    Double getTotalCost(@RequestBody OrderDto order);

    @PostMapping
    PaymentDto payment(@RequestBody OrderDto order);

    @PostMapping("/refund")
    void paymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/failed")
    void paymentFailed(@RequestBody UUID paymentId);
}