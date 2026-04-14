package ru.yandex.practicum.shoppingcart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.shoppingcart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.shoppingcart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.shoppingcart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.shoppingcart.model.ShoppingCart;
import ru.yandex.practicum.shoppingcart.repository.ShoppingCartRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartMapper cartMapper;
    private final WarehouseClient warehouseClient;

    private ShoppingCart getOrCreateActiveCart(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Username must not be empty");
        }

        return cartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> createNewCart(username));
    }

    private ShoppingCart createNewCart(String username) {
        ShoppingCart newCart = new ShoppingCart();
        newCart.setUsername(username);
        newCart.setActive(true);
        newCart.setProducts(new java.util.HashMap<>());
        return cartRepository.save(newCart);
    }

    public ShoppingCartDto getShoppingCart(String username) {
        log.debug("Getting shopping cart for user: {}", username);
        ShoppingCart cart = getOrCreateActiveCart(username);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        log.debug("Adding products to cart for user: {}, products: {}", username, products);

        ShoppingCart cart = getOrCreateActiveCart(username);

        Map<UUID, Long> originalProducts = new HashMap<>(cart.getProducts());

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();
            cart.getProducts().merge(productId, quantity, Long::sum);
        }

        try {
            ShoppingCartDto cartDto = cartMapper.toDto(cart);
            warehouseClient.checkProductQuantityEnoughForShoppingCart(cartDto);
        } catch (Exception e) {
            cart.setProducts(originalProducts);
            log.error("Failed to check products availability: {}", e.getMessage());
            throw e;
        }

        ShoppingCart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }

    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        log.debug("Removing products from cart for user: {}, products: {}", username, productIds);
        ShoppingCart cart = getOrCreateActiveCart(username);

        boolean anyRemoved = false;
        for (UUID productId : productIds) {
            if (cart.getProducts().remove(productId) != null) {
                anyRemoved = true;
            }
        }

        if (!anyRemoved) {
            throw new NoProductsInShoppingCartException("None of the specified products were found in the cart");
        }

        ShoppingCart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, UUID productId, Long newQuantity) {
        log.debug("Changing product quantity for user: {}, product: {}, newQuantity: {}", username, productId, newQuantity);
        ShoppingCart cart = getOrCreateActiveCart(username);

        if (!cart.getProducts().containsKey(productId)) {
            throw new NoProductsInShoppingCartException("Product not found in cart: " + productId);
        }

        if (newQuantity <= 0) {
            cart.getProducts().remove(productId);
        } else {
            cart.getProducts().put(productId, newQuantity);
        }

        ShoppingCart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }

    @Transactional
    public void deactivateCurrentShoppingCart(String username) {
        log.debug("Deactivating shopping cart for user: {}", username);
        ShoppingCart cart = getOrCreateActiveCart(username);
        cart.setActive(false);
        cartRepository.save(cart);
    }
}