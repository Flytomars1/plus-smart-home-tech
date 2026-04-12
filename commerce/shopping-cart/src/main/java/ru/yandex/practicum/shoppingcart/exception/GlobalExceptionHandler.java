package ru.yandex.practicum.shoppingcart.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleNotAuthorizedUser(NotAuthorizedUserException e) {
        log.error("Not authorized: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleNoProductsInShoppingCart(NoProductsInShoppingCartException e) {
        log.error("No products in cart: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }
}