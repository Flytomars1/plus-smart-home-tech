package ru.yandex.practicum.collector.mapper;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public interface SensorEventMappable {
    SensorEventAvro toAvro();
}