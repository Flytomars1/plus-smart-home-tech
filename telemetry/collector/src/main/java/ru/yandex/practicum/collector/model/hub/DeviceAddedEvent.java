package ru.yandex.practicum.collector.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceAddedEvent extends HubEvent {
    private String id;
    private DeviceType deviceType;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED_EVENT;
    }

    @Override
    public HubEventAvro toAvro() {
        return HubEventAvro.newBuilder()
                .setHubId(getHubId())
                .setTimestamp(getTimestamp())
                .setPayload(
                        DeviceAddedEventAvro.newBuilder()
                                .setId(id)
                                .setType(DeviceTypeAvro.valueOf(deviceType.name()))
                                .build()
                )
                .build();
    }
}