package ru.yandex.practicum.collector.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceRemovedEvent extends HubEvent {
    private String id;

    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_REMOVED_EVENT;
    }

    @Override
    public HubEventAvro toAvro() {
        return HubEventAvro.newBuilder()
                .setHubId(getHubId())
                .setTimestamp(getTimestamp())
                .setPayload(
                        DeviceRemovedEventAvro.newBuilder()
                                .setId(id)
                                .build()
                )
                .build();
    }
}