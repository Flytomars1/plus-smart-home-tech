package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class SetProductQuantityStateRequest {
    private UUID productId;
    private QuantityState quantityState;
}