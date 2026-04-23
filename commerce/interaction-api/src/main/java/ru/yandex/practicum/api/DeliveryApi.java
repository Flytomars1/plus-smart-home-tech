package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.DeliveryCostRequest;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.DeliveryRequest;

import java.math.BigDecimal;
import java.util.UUID;

@RequestMapping("/api/v1/delivery")
public interface DeliveryApi {

    @PutMapping
    DeliveryDto planDelivery(@RequestBody DeliveryRequest request);

    @PostMapping("/cost")
    BigDecimal deliveryCost(@RequestBody DeliveryCostRequest request);

    @PostMapping("/picked")
    void deliveryPicked(@RequestBody UUID orderId);

    @PostMapping("/successful")
    void deliverySuccessful(@RequestBody UUID orderId);

    @PostMapping("/failed")
    void deliveryFailed(@RequestBody UUID orderId);
}