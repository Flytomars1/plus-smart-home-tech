package ru.yandex.practicum.collector.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {
    private String name;
    private List<ScenarioCondition> conditions;
    private List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED_EVENT;
    }

    @Override
    public HubEventAvro toAvro() {
        return HubEventAvro.newBuilder()
                .setHubId(getHubId())
                .setTimestamp(getTimestamp())
                .setPayload(
                        ScenarioAddedEventAvro.newBuilder()
                                .setName(name)
                                .setConditions(conditions.stream()
                                        .map(this::toScenarioConditionAvro)
                                        .toList())
                                .setActions(actions.stream()
                                        .map(this::toDeviceActionAvro)
                                        .toList())
                                .build()
                )
                .build();
    }

    private ScenarioConditionAvro toScenarioConditionAvro(ScenarioCondition condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getValue())
                .build();
    }

    private DeviceActionAvro toDeviceActionAvro(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}