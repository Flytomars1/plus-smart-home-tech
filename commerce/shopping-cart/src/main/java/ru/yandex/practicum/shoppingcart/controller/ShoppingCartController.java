package ru.yandex.practicum.shoppingcart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.ShoppingCartApi;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.shoppingcart.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartApi {
    private final ShoppingCartService shoppingCartService;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        return shoppingCartService.removeFromShoppingCart(username, productIds);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        return shoppingCartService.changeProductQuantity(
                username,
                request.getProductId(),
                request.getNewQuantity()
        );
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        shoppingCartService.deactivateCurrentShoppingCart(username);
    }
}