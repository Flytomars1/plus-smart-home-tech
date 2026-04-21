package ru.yandex.practicum.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.dto.DeliveryState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deliveryId;

    @Embedded
    private Address fromAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "to_country")),
            @AttributeOverride(name = "city", column = @Column(name = "to_city")),
            @AttributeOverride(name = "street", column = @Column(name = "to_street")),
            @AttributeOverride(name = "house", column = @Column(name = "to_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "to_flat"))
    })
    private Address toAddress;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private DeliveryState deliveryState;

    private Double weight;

    private Double volume;

    private Boolean fragile;

    private BigDecimal deliveryPrice;
}