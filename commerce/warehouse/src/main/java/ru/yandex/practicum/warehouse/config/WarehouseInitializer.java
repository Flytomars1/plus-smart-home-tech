package ru.yandex.practicum.warehouse.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.warehouse.model.WarehouseAddress;
import ru.yandex.practicum.warehouse.repository.WarehouseAddressRepository;

import java.security.SecureRandom;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarehouseInitializer {
    private final WarehouseAddressRepository addressRepository;

    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    @EventListener(ApplicationReadyEvent.class)
    public void initWarehouseAddress() {
        if (addressRepository.count() == 0) {
            log.info("Initializing warehouse address with: {}", CURRENT_ADDRESS);

            WarehouseAddress address = new WarehouseAddress();
            address.setCountry(CURRENT_ADDRESS);
            address.setCity(CURRENT_ADDRESS);
            address.setStreet(CURRENT_ADDRESS);
            address.setHouse(CURRENT_ADDRESS);
            address.setFlat(CURRENT_ADDRESS);

            addressRepository.save(address);
            log.info("Warehouse address initialized successfully");
        }
    }
}