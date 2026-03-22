package ru.yandex.practicum.analyzer.model.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class SnapshotData {

    private String hubId;
    private Instant timestamp;
    private Map<String, SensorStateAvro> sensorsState;

    public static SnapshotData fromAvro(SensorsSnapshotAvro snapshot) {
        return SnapshotData.builder()
                .hubId(snapshot.getHubId())
                .timestamp(snapshot.getTimestamp())
                .sensorsState(snapshot.getSensorsState())
                .build();
    }
}