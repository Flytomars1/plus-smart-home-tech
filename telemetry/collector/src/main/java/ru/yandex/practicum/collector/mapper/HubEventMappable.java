package ru.yandex.practicum.collector.mapper;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventMappable {
    HubEventAvro toAvro();
}