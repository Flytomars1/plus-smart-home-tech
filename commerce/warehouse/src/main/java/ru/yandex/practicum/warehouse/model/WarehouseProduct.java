package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.dto.DimensionDto;

import java.util.UUID;

@Data
@Entity
@Table(name = "warehouse_products")
public class WarehouseProduct {
    @Id
    private UUID productId;

    private Boolean fragile;

    @Embedded
    private DimensionDto dimension;

    private Double weight;

    private Long quantity = 0L;
}