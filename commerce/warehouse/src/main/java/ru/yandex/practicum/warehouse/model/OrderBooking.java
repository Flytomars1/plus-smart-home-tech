package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBooking {

    @Id
    private UUID orderId;

    private UUID deliveryId;

    @ElementCollection
    @CollectionTable(name = "order_booking_products",
            joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Long> products;

    private Double deliveryWeight;

    private Double deliveryVolume;

    private Boolean fragile;
}