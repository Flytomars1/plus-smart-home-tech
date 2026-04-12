package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AddProductToWarehouseRequest {
    private UUID productId;
    private Long quantity;
}