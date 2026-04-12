package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ChangeProductQuantityRequest {
    private UUID productId;
    private Long newQuantity;
}