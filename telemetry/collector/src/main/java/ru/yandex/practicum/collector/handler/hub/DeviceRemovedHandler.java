package ru.yandex.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.handler.HubEventHandler;
import ru.yandex.practicum.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRemovedHandler implements HubEventHandler {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        var payload = event.getDeviceRemoved();

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        log.info("Обработка удаления устройства: hubId={}, deviceId={}",
                event.getHubId(), payload.getId());

        DeviceRemovedEvent deviceEvent = new DeviceRemovedEvent();
        deviceEvent.setHubId(event.getHubId());
        deviceEvent.setTimestamp(timestamp);
        deviceEvent.setId(payload.getId());

        kafkaEventProducer.sendHubEvent(deviceEvent);
    }
}