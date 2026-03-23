package ru.yandex.practicum.aggregator.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class SnapshotManager {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId().toString();
        String sensorId = event.getId().toString();
        Instant eventTimestamp = event.getTimestamp();

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);

        if (snapshot == null) {
            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(eventTimestamp)
                    .setSensorsState(new HashMap<>())
                    .build();
            snapshots.put(hubId, snapshot);
        }

        SensorStateAvro oldState = snapshot.getSensorsState().get(sensorId);

        if (oldState != null) {
            Instant oldTimestamp = oldState.getTimestamp();

            if (eventTimestamp.isBefore(oldTimestamp)) {
                log.debug("Игнорируем устаревшее событие для датчика {}: old={}, new={}",
                        sensorId, oldTimestamp, eventTimestamp);
                return Optional.empty();
            }

            if (eventTimestamp.equals(oldTimestamp) && oldState.getData().equals(event.getPayload())) {
                log.debug("Игнорируем дубликат события для датчика {}", sensorId);
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventTimestamp)
                .setData(event.getPayload())
                .build();

        snapshot.getSensorsState().put(sensorId, newState);
        snapshot.setTimestamp(eventTimestamp);

        log.info("Обновлен снапшот для хаба {}: датчик {} -> {}",
                hubId, sensorId, event.getPayload().getClass().getSimpleName());

        return Optional.of(snapshot);
    }
}