package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/api/v1/shopping-cart")
public interface ShoppingCartApi {

    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam String username);

    @PutMapping
    ShoppingCartDto addProductToShoppingCart(
            @RequestParam String username,
            @RequestBody Map<UUID, Long> products
    );

    @PostMapping("/remove")
    ShoppingCartDto removeFromShoppingCart(
            @RequestParam String username,
            @RequestBody List<UUID> productIds
    );

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(
            @RequestParam String username,
            @RequestBody ChangeProductQuantityRequest request
    );

    @DeleteMapping
    void deactivateCurrentShoppingCart(@RequestParam String username);
}