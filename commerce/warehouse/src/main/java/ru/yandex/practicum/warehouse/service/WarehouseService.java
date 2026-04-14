package ru.yandex.practicum.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.warehouse.model.WarehouseAddress;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.WarehouseAddressRepository;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {
    private final WarehouseProductRepository productRepository;
    private final WarehouseAddressRepository addressRepository;

    @Transactional
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.debug("Adding new product to warehouse: {}", request);

        if (productRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Product already exists in warehouse: " + request.getProductId()
            );
        }

        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(request.getProductId());
        product.setFragile(request.getFragile());
        product.setDimension(request.getDimension());
        product.setWeight(request.getWeight());
        product.setQuantity(0L);

        productRepository.save(product);
    }

    @Transactional
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        log.debug("Adding product quantity to warehouse: {}", request);

        WarehouseProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                        "Product not found in warehouse: " + request.getProductId()
                ));

        product.setQuantity(product.getQuantity() + request.getQuantity());
        productRepository.save(product);
    }

    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
        log.debug("Checking product quantity for shopping cart: {}", cart);

        Map<UUID, Long> insufficientProducts = new HashMap<>();
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (Map.Entry<UUID, Long> entry : cart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requestedQuantity = entry.getValue();

            WarehouseProduct product = productRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                            "Product not found in warehouse: " + productId
                    ));

            if (product.getQuantity() < requestedQuantity) {
                insufficientProducts.put(productId, product.getQuantity());
            }

            totalWeight += product.getWeight() * requestedQuantity;

            DimensionDto dim = product.getDimension();
            double volume = dim.getWidth() * dim.getHeight() * dim.getDepth() * requestedQuantity;
            totalVolume += volume;

            if (product.getFragile()) {
                hasFragile = true;
            }
        }

        if (!insufficientProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    "Insufficient quantity for some products", insufficientProducts
            );
        }

        BookedProductsDto result = new BookedProductsDto();
        result.setDeliveryWeight(totalWeight);
        result.setDeliveryVolume(totalVolume);
        result.setFragile(hasFragile);

        return result;
    }

    public AddressDto getWarehouseAddress() {
        WarehouseAddress address = addressRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Warehouse address not configured"));

        return address.toDto();
    }
}