package ru.yandex.practicum.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.delivery.config.WarehouseProps;
import ru.yandex.practicum.delivery.exception.NoDeliveryFoundException;
import ru.yandex.practicum.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.delivery.model.Address;
import ru.yandex.practicum.delivery.model.Delivery;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.WarehouseClient;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;
    private final WarehouseProps warehouseProps;
    private final DeliveryMapper deliveryMapper;

    private static final double BASE_COST = 5.0;

    public BigDecimal deliveryCost(OrderDto order, AddressDto deliveryAddress) {
        log.info("Calculating delivery cost for order: {}, address: {}", order.getOrderId(), deliveryAddress);

        AddressDto warehouseAddress = getWarehouseAddress();
        double cost = BASE_COST;

        if ("ADDRESS_2".equals(warehouseAddress.getStreet())) {
            cost = BASE_COST * 2 + BASE_COST;
        } else {
            cost = BASE_COST * 1 + BASE_COST;
        }
        log.debug("After warehouse address factor: {}", cost);

        if (order.getFragile() != null && order.getFragile()) {
            double fragileAddition = cost * 0.2;
            cost = cost + fragileAddition;
            log.debug("After fragile addition: {} (+{})", cost, fragileAddition);
        }

        if (order.getDeliveryWeight() != null) {
            double weightAddition = order.getDeliveryWeight() * 0.3;
            cost = cost + weightAddition;
            log.debug("After weight addition: {} (+{})", cost, weightAddition);
        }

        if (order.getDeliveryVolume() != null) {
            double volumeAddition = order.getDeliveryVolume() * 0.2;
            cost = cost + volumeAddition;
            log.debug("After volume addition: {} (+{})", cost, volumeAddition);
        }

        if (deliveryAddress != null && !warehouseAddress.getStreet().equals(deliveryAddress.getStreet())) {
            double addressAddition = cost * 0.2;
            cost = cost + addressAddition;
            log.debug("After address addition (different street): {} (+{})", cost, addressAddition);
        } else {
            log.debug("Address street matches warehouse street, no addition");
        }

        log.info("Delivery cost calculated: {}", cost);
        return BigDecimal.valueOf(cost);
    }

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto, OrderDto order) {
        log.info("Planning delivery for order: {}", deliveryDto.getOrderId());

        Address warehouseAddress = deliveryMapper.toAddress(getWarehouseAddress());
        Address toAddress = deliveryMapper.toAddress(deliveryDto.getToAddress());

        BigDecimal deliveryPrice = deliveryCost(order, deliveryDto.getToAddress());

        Delivery delivery = Delivery.builder()
                .deliveryId(deliveryDto.getDeliveryId())
                .fromAddress(warehouseAddress)
                .toAddress(toAddress)
                .orderId(deliveryDto.getOrderId())
                .deliveryState(DeliveryState.CREATED)
                .weight(order.getDeliveryWeight())
                .volume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .deliveryPrice(deliveryPrice)
                .build();

        delivery = deliveryRepository.save(delivery);
        log.info("Delivery created with id: {}, price: {}", delivery.getDeliveryId(), deliveryPrice);

        // Используем маппер для конвертации в DTO
        return deliveryMapper.toDeliveryDto(delivery);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        log.info("Delivery picked for order: {}", orderId);

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена для заказа: " + orderId));

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        ShippedToDeliveryRequest request = ShippedToDeliveryRequest.builder()
                .orderId(orderId)
                .deliveryId(delivery.getDeliveryId())
                .build();
        warehouseClient.shippedToDelivery(request);
        log.info("Delivery status updated to IN_PROGRESS for order: {}", orderId);
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        log.info("Delivery successful for order: {}", orderId);

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена для заказа: " + orderId));

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.delivery(orderId);

        log.info("Delivery status updated to DELIVERED for order: {}", orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        log.info("Delivery failed for order: {}", orderId);

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена для заказа: " + orderId));

        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(orderId);
        log.info("Delivery status updated to FAILED for order: {}", orderId);
    }

    private AddressDto getWarehouseAddress() {
        return AddressDto.builder()
                .country(warehouseProps.getCountry())
                .city(warehouseProps.getCity())
                .street(warehouseProps.getStreet())
                .house(warehouseProps.getHouse())
                .flat(warehouseProps.getFlat())
                .build();
    }
}