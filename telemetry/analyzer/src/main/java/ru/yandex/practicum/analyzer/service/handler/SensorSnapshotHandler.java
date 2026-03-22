package ru.yandex.practicum.analyzer.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.service.ScenarioExecutor;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorSnapshotHandler {

    private final ScenarioExecutor scenarioExecutor;

    public void handle(SensorsSnapshotAvro snapshot) {
        log.debug("Обработка снапшота для хаба: {}", snapshot.getHubId());
        scenarioExecutor.executeScenarios(snapshot);
    }
}