package ru.yandex.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.handler.HubEventHandler;
import ru.yandex.practicum.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.collector.model.hub.DeviceType;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedHandler implements HubEventHandler {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        var payload = event.getDeviceAdded();

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        log.info("Обработка добавления устройства: hubId={}, deviceId={}, type={}",
                event.getHubId(), payload.getId(), payload.getType());

        DeviceAddedEvent deviceEvent = new DeviceAddedEvent();
        deviceEvent.setHubId(event.getHubId());
        deviceEvent.setTimestamp(timestamp);
        deviceEvent.setId(payload.getId());
        deviceEvent.setDeviceType(mapDeviceType(payload.getType()));

        kafkaEventProducer.sendHubEvent(deviceEvent);
    }

    private DeviceType mapDeviceType(ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto type) {
        return switch (type) {
            case MOTION_SENSOR -> DeviceType.MOTION_SENSOR;
            case TEMPERATURE_SENSOR -> DeviceType.TEMPERATURE_SENSOR;
            case LIGHT_SENSOR -> DeviceType.LIGHT_SENSOR;
            case CLIMATE_SENSOR -> DeviceType.CLIMATE_SENSOR;
            case SWITCH_SENSOR -> DeviceType.SWITCH_SENSOR;
            default -> throw new IllegalArgumentException("Unknown device type: " + type);
        };
    }
}