package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

@FeignClient(name = "payment")
public interface PaymentClient {

    @PostMapping("/api/v1/payment/productCost")
    Double productCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/totalCost")
    Double getTotalCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment")
    PaymentDto payment(@RequestBody OrderDto order);
}