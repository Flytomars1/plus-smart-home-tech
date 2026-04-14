package ru.yandex.practicum.warehouse.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleProductAlreadyExists(SpecifiedProductAlreadyInWarehouseException e) {
        log.error("Product already exists: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleProductNotFound(NoSpecifiedProductInWarehouseException e) {
        log.error("Product not found: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleInsufficientQuantity(ProductInShoppingCartLowQuantityInWarehouse e) {
        log.error("Insufficient quantity: {}", e.getMessage());
        return Map.of(
                "error", e.getMessage(),
                "insufficientProducts", e.getInsufficientProducts()
        );
    }
}