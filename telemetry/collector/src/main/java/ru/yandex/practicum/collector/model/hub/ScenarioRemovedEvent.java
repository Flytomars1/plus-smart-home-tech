package ru.yandex.practicum.collector.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED_EVENT;
    }

    @Override
    public HubEventAvro toAvro() {
        return HubEventAvro.newBuilder()
                .setHubId(getHubId())
                .setTimestamp(getTimestamp())
                .setPayload(
                        ScenarioRemovedEventAvro.newBuilder()
                                .setName(name)
                                .build()
                )
                .build();
    }
}