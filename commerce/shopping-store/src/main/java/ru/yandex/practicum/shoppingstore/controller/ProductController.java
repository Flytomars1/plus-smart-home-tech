package ru.yandex.practicum.shoppingstore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.ShoppingStoreApi;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.QuantityState;
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
    public Page<ProductDto> getProducts(ProductCategory category, int page, int size, List<String> sort) {
        log.debug("Getting products - category: {}, page: {}, size: {}, sort: {}", category, page, size, sort);

        Sort sortOrder = Sort.by(Sort.Direction.ASC, "productName");
        if (sort != null && !sort.isEmpty()) {
            String sortParam = sort.get(0);
            String[] parts = sortParam.trim().split(",");
            String property = parts[0].trim();

            Sort.Direction direction = Sort.Direction.ASC;
            if (parts.length > 1) {
                String directionStr = parts[1].trim().toUpperCase();
                if ("DESC".equals(directionStr)) {
                    direction = Sort.Direction.DESC;
                }
            }
            sortOrder = Sort.by(direction, property);
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return productService.getProducts(category, pageable);
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
    public boolean setProductQuantityState(@RequestParam UUID productId, @RequestParam QuantityState quantityState) {
        log.debug("Setting product quantity state - productId: {}, quantityState: {}", productId, quantityState);
        SetProductQuantityStateRequest request = new SetProductQuantityStateRequest();
        request.setProductId(productId);
        request.setQuantityState(quantityState);
        return productService.setProductQuantityState(request);
    }
}