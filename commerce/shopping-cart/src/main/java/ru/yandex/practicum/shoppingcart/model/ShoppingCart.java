package ru.yandex.practicum.shoppingcart.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID shoppingCartId;

    private String username;

    private boolean active = true;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<UUID, Long> products = new HashMap<>();
}