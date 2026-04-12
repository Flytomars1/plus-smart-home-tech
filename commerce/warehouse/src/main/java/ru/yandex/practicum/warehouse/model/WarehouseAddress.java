package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.dto.AddressDto;

@Data
@Entity
@Table(name = "warehouse_address")
public class WarehouseAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;

    public AddressDto toDto() {
        AddressDto dto = new AddressDto();
        dto.setCountry(country);
        dto.setCity(city);
        dto.setStreet(street);
        dto.setHouse(house);
        dto.setFlat(flat);
        return dto;
    }
}