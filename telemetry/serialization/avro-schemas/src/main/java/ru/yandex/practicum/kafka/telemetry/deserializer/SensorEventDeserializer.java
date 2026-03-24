package ru.yandex.practicum.aggregator.service.deserializer;

import ru.yandex.practicum.kafka.telemetry.deserializer.BaseAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public class SensorEventDeserializer extends BaseAvroDeserializer<SensorEventAvro> {

    public SensorEventDeserializer() {
        super(SensorEventAvro.getClassSchema());
    }
}