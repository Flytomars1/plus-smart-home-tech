package ru.yandex.practicum.delivery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.DeliveryApi;
import ru.yandex.practicum.dto.DeliveryCostRequest;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.DeliveryRequest;
import ru.yandex.practicum.delivery.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto planDelivery(DeliveryRequest request) {
        UUID orderId = request.getDeliveryDto().getOrderId();
        log.info("PUT /api/v1/delivery - orderId: {}", orderId);
        return deliveryService.planDelivery(request.getDeliveryDto(), request.getOrder());
    }

    @Override
    public BigDecimal deliveryCost(DeliveryCostRequest request) {
        log.info("POST /api/v1/delivery/cost - orderId: {}", request.getOrder().getOrderId());
        return deliveryService.deliveryCost(request.getOrder(), request.getDeliveryAddress());
    }

    @Override
    public void deliveryPicked(UUID orderId) {
        log.info("POST /api/v1/delivery/picked - orderId: {}", orderId);
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    public void deliverySuccessful(UUID orderId) {
        log.info("POST /api/v1/delivery/successful - orderId: {}", orderId);
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    public void deliveryFailed(UUID orderId) {
        log.info("POST /api/v1/delivery/failed - orderId: {}", orderId);
        deliveryService.deliveryFailed(orderId);
    }
}