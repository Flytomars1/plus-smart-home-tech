package ru.yandex.practicum.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.warehouse.model.WarehouseAddress;

public interface WarehouseAddressRepository extends JpaRepository<WarehouseAddress, Long> {
}