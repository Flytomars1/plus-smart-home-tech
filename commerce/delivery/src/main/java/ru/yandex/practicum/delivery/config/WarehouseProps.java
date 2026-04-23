package ru.yandex.practicum.delivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "delivery.warehouse.address")
public class WarehouseProps {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}