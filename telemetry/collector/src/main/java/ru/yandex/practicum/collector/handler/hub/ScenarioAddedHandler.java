package ru.yandex.practicum.collector.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.handler.HubEventHandler;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedHandler implements HubEventHandler {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        var payload = event.getScenarioAdded();

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        log.info("Обработка добавления сценария: hubId={}, name={}",
                event.getHubId(), payload.getName());

        ScenarioAddedEvent scenarioEvent = new ScenarioAddedEvent();
        scenarioEvent.setHubId(event.getHubId());
        scenarioEvent.setTimestamp(timestamp);
        scenarioEvent.setName(payload.getName());
        scenarioEvent.setConditions(mapConditions(payload.getConditionsList()));
        scenarioEvent.setActions(mapActions(payload.getActionsList()));

        kafkaEventProducer.sendHubEvent(scenarioEvent);
    }

    private List<ScenarioCondition> mapConditions(List<ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto> conditions) {
        List<ScenarioCondition> result = new ArrayList<>();
        for (var c : conditions) {
            ScenarioCondition condition = new ScenarioCondition();
            condition.setSensorId(c.getSensorId());
            condition.setType(mapConditionType(c.getType()));
            condition.setOperation(mapConditionOperation(c.getOperation()));

            switch (c.getValueCase()) {
                case INT_VALUE:
                    condition.setValue(c.getIntValue());
                    break;
                case BOOL_VALUE:
                    condition.setValue(c.getBoolValue());
                    break;
                default:
                    condition.setValue(null);
            }

            result.add(condition);
        }
        return result;
    }

    private List<DeviceAction> mapActions(List<ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto> actions) {
        List<DeviceAction> result = new ArrayList<>();
        for (var a : actions) {
            DeviceAction action = new DeviceAction();
            action.setSensorId(a.getSensorId());
            action.setType(mapActionType(a.getType()));
            action.setValue(a.hasValue() ? a.getValue() : null);
            result.add(action);
        }
        return result;
    }

    private ConditionType mapConditionType(ru.yandex.practicum.grpc.telemetry.event.ConditionTypeProto type) {
        return switch (type) {
            case MOTION -> ConditionType.MOTION;
            case LUMINOSITY -> ConditionType.LUMINOSITY;
            case SWITCH -> ConditionType.SWITCH;
            case TEMPERATURE -> ConditionType.TEMPERATURE;
            case CO2LEVEL -> ConditionType.CO2LEVEL;
            case HUMIDITY -> ConditionType.HUMIDITY;
            default -> throw new IllegalArgumentException("Unknown condition type: " + type);
        };
    }

    private ConditionOperation mapConditionOperation(ru.yandex.practicum.grpc.telemetry.event.ConditionOperationProto op) {
        return switch (op) {
            case EQUALS -> ConditionOperation.EQUALS;
            case GREATER_THAN -> ConditionOperation.GREATER_THAN;
            case LOWER_THAN -> ConditionOperation.LOWER_THAN;
            default -> throw new IllegalArgumentException("Unknown operation: " + op);
        };
    }

    private ActionType mapActionType(ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto type) {
        return switch (type) {
            case ACTIVATE -> ActionType.ACTIVATE;
            case DEACTIVATE -> ActionType.DEACTIVATE;
            case INVERSE -> ActionType.INVERSE;
            case SET_VALUE -> ActionType.SET_VALUE;
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };
    }
}