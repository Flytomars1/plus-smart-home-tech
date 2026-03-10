package ru.yandex.practicum.collector.service;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@UtilityClass
public class AvroMapper {

    public SensorEventAvro toSensorEventAvro(SensorEvent event) {
        return event.toAvro();
    }

    public HubEventAvro toHubEventAvro(HubEvent event) {
        return event.toAvro();
    }
}