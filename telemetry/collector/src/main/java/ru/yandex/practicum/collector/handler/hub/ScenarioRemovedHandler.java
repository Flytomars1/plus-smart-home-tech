package ru.yandex.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.handler.HubEventHandler;
import ru.yandex.practicum.collector.model.hub.ScenarioRemovedEvent;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedHandler implements HubEventHandler {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        var payload = event.getScenarioRemoved();

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        log.info("Обработка удаления сценария: hubId={}, name={}",
                event.getHubId(), payload.getName());

        ScenarioRemovedEvent scenarioEvent = new ScenarioRemovedEvent();
        scenarioEvent.setHubId(event.getHubId());
        scenarioEvent.setTimestamp(timestamp);
        scenarioEvent.setName(payload.getName());

        kafkaEventProducer.sendHubEvent(scenarioEvent);
    }
}