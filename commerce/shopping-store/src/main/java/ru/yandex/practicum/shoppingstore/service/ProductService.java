package ru.yandex.practicum.shoppingstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.ProductState;
import ru.yandex.practicum.dto.QuantityState;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.shoppingstore.exception.ProductNotFoundException;
import ru.yandex.practicum.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.shoppingstore.model.Product;
import ru.yandex.practicum.shoppingstore.repository.ProductRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        log.debug("Getting products by category: {}, pageable: {}", category, pageable);
        Page<Product> products = productRepository.findByProductCategoryAndProductState(
                category, ProductState.ACTIVE, pageable
        );
        return products.map(productMapper::toDto);
    }

    public ProductDto getProduct(UUID productId) {
        log.debug("Getting product by id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createNewProduct(ProductDto productDto) {
        log.debug("Creating new product: {}", productDto);
        Product product = productMapper.toEntity(productDto);

        if (productDto.getProductState() != null) {
            product.setProductState(productDto.getProductState());
        } else {
            product.setProductState(ProductState.ACTIVE);
        }

        if (productDto.getQuantityState() != null) {
            product.setQuantityState(productDto.getQuantityState());
        } else {
            product.setQuantityState(QuantityState.ENOUGH);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        log.debug("Updating product: {}", productDto);
        Product existingProduct = productRepository.findById(productDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productDto.getProductId()));

        productMapper.updateEntity(existingProduct, productDto);
        if (productDto.getQuantityState() != null) {
            existingProduct.setQuantityState(productDto.getQuantityState());
        }
        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    public boolean removeProductFromStore(UUID productId) {
        log.debug("Removing product from store: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        log.debug("Setting quantity state for product: {}", request);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));
        product.setQuantityState(request.getQuantityState());
        productRepository.save(product);
        return true;
    }
}