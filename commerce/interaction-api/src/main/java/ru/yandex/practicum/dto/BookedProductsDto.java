package ru.yandex.practicum.dto;

import lombok.Data;

@Data
public class BookedProductsDto {
    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
}