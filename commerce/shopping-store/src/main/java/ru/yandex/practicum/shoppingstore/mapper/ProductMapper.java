package ru.yandex.practicum.shoppingstore.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.shoppingstore.model.Product;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setImageSrc(product.getImageSrc());
        dto.setQuantityState(product.getQuantityState());
        dto.setProductState(product.getProductState());
        dto.setProductCategory(product.getProductCategory());
        dto.setPrice(product.getPrice());
        return dto;
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setImageSrc(dto.getImageSrc());
        product.setProductCategory(dto.getProductCategory());
        product.setPrice(dto.getPrice());
        return product;
    }

    public void updateEntity(Product product, ProductDto dto) {
        if (dto.getProductName() != null) {
            product.setProductName(dto.getProductName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getImageSrc() != null) {
            product.setImageSrc(dto.getImageSrc());
        }
        if (dto.getProductCategory() != null) {
            product.setProductCategory(dto.getProductCategory());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
    }
}