package ru.yandex.practicum.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.delivery.model.Address;
import ru.yandex.practicum.delivery.model.Delivery;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;

@Component
public class DeliveryMapper {

    public AddressDto toAddressDto(Address address) {
        if (address == null) return null;

        return AddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .flat(address.getFlat())
                .build();
    }

    public Address toAddress(AddressDto addressDto) {
        if (addressDto == null) return null;

        return Address.builder()
                .country(addressDto.getCountry())
                .city(addressDto.getCity())
                .street(addressDto.getStreet())
                .house(addressDto.getHouse())
                .flat(addressDto.getFlat())
                .build();
    }

    public DeliveryDto toDeliveryDto(Delivery delivery) {
        if (delivery == null) return null;

        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .fromAddress(toAddressDto(delivery.getFromAddress()))
                .toAddress(toAddressDto(delivery.getToAddress()))
                .orderId(delivery.getOrderId())
                .deliveryState(delivery.getDeliveryState())
                .build();
    }

    public Address toWarehouseAddress(String country, String city, String street, String house, String flat) {
        return Address.builder()
                .country(country)
                .city(city)
                .street(street)
                .house(house)
                .flat(flat)
                .build();
    }
}