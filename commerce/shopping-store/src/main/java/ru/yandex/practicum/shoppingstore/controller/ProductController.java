package ru.yandex.practicum.shoppingstore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.ShoppingStoreApi;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.shoppingstore.service.ProductService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductController implements ShoppingStoreApi {
    private final ProductService productService;

    @Override
    public List<ProductDto> getProducts(ProductCategory category) {
        log.debug("Getting products by category: {}", category);
        return productService.getProducts(category);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return productService.getProduct(productId);
    }

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        return productService.createNewProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    @Override
    public boolean removeProductFromStore(UUID productId) {
        return productService.removeProductFromStore(productId);
    }

    @Override
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        return productService.setProductQuantityState(request);
    }
}