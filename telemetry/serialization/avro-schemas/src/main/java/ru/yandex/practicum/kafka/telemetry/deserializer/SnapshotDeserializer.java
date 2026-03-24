package ru.yandex.practicum.analyzer.service.deserializer;

import ru.yandex.practicum.kafka.telemetry.deserializer.BaseAvroDeserializer;  // ← добавить этот импорт
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public class SnapshotDeserializer extends BaseAvroDeserializer<SensorsSnapshotAvro> {

    public SnapshotDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }
}