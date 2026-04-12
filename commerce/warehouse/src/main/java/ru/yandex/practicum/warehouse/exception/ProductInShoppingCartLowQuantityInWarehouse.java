package ru.yandex.practicum.warehouse.exception;

import java.util.Map;
import java.util.UUID;

public class ProductInShoppingCartLowQuantityInWarehouse extends RuntimeException {
    private final Map<UUID, Long> insufficientProducts;

    public ProductInShoppingCartLowQuantityInWarehouse(String message, Map<UUID, Long> insufficientProducts) {
        super(message);
        this.insufficientProducts = insufficientProducts;
    }

    public Map<UUID, Long> getInsufficientProducts() {
        return insufficientProducts;
    }
}