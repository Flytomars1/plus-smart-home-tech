package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/shopping-store")
public interface ShoppingStoreApi {

    @GetMapping
    List<ProductDto> getProducts(@RequestParam ProductCategory category);

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);

    @PutMapping
    ProductDto createNewProduct(@RequestBody ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@RequestBody ProductDto productDto);

    @PostMapping("/removeProductFromStore")
    boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    boolean setProductQuantityState(@RequestBody SetProductQuantityStateRequest request);
}