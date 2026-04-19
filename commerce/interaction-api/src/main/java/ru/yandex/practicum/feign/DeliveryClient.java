package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.DeliveryCostRequest;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.DeliveryRequest;


@FeignClient(name = "delivery")
public interface DeliveryClient {

    @PostMapping("/api/v1/delivery/cost")
    Double deliveryCost(@RequestBody DeliveryCostRequest request);

    @PutMapping("/api/v1/delivery")
    DeliveryDto planDelivery(@RequestBody DeliveryRequest request);
}