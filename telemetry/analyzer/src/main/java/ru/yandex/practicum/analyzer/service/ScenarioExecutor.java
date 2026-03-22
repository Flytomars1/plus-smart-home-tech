package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.analyzer.client.HubRouterClient;
import ru.yandex.practicum.analyzer.model.dto.SnapshotData;
import ru.yandex.practicum.analyzer.model.entity.Action;
import ru.yandex.practicum.analyzer.model.entity.Condition;
import ru.yandex.practicum.analyzer.model.entity.Scenario;
import ru.yandex.practicum.analyzer.model.repository.ScenarioRepository;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioExecutor {

    private final ScenarioRepository scenarioRepository;
    private final HubRouterClient hubRouterClient;

    public void executeScenarios(SensorsSnapshotAvro snapshot) {
        SnapshotData snapshotData = SnapshotData.fromAvro(snapshot);
        String hubId = snapshotData.getHubId();

        // Загружаем все сценарии для этого хаба
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);

        for (Scenario scenario : scenarios) {
            if (checkConditions(scenario.getConditions(), snapshotData)) {
                log.info("Сценарий '{}' активирован для хаба {}", scenario.getName(), hubId);
                executeActions(scenario.getActions(), scenario.getName(), hubId);
            }
        }
    }

    private boolean checkConditions(List<Condition> conditions, SnapshotData snapshot) {
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        return conditions.stream().allMatch(condition -> {
            SensorStateAvro sensorState = sensorsState.get(condition.getSensorId());
            if (sensorState == null) {
                log.debug("Датчик {} не найден в снапшоте", condition.getSensorId());
                return false;
            }

            return evaluateCondition(condition, sensorState);
        });
    }

    private boolean evaluateCondition(Condition condition, SensorStateAvro sensorState) {
        Object sensorValue = extractSensorValue(sensorState.getData());
        if (sensorValue == null) {
            return false;
        }

        Integer conditionValue = condition.getValue();
        if (conditionValue == null && !(sensorValue instanceof Boolean)) {
            return false;
        }

        String operation = condition.getOperation();

        switch (operation) {
            case "EQUALS":
                if (sensorValue instanceof Boolean) {
                    return (boolean) sensorValue == (conditionValue == 1);
                }
                return sensorValue.equals(conditionValue);
            case "GREATER_THAN":
                if (sensorValue instanceof Integer) {
                    return (Integer) sensorValue > conditionValue;
                }
                return false;
            case "LOWER_THAN":
                if (sensorValue instanceof Integer) {
                    return (Integer) sensorValue < conditionValue;
                }
                return false;
            default:
                return false;
        }
    }

    private Object extractSensorValue(Object data) {
        if (data instanceof ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro) {
            return ((ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro) data).getMotion();
        }
        if (data instanceof ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro) {
            return ((ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro) data).getLuminosity();
        }
        if (data instanceof ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro) {
            return ((ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro) data).getTemperatureC();
        }
        if (data instanceof ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro) {
            return ((ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro) data).getTemperatureC();
        }
        if (data instanceof ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro) {
            return ((ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro) data).getState();
        }
        return null;
    }

    private void executeActions(List<Action> actions, String scenarioName, String hubId) {
        for (Action action : actions) {
            DeviceActionProto deviceAction = DeviceActionProto.newBuilder()
                    .setSensorId(action.getSensorId())
                    .setType(ActionTypeProto.valueOf(action.getType()))
                    .setValue(action.getValue() != null ? action.getValue() : 0)
                    .build();

            hubRouterClient.sendAction(hubId, scenarioName, deviceAction);
            log.info("Отправлено действие: sensorId={}, type={}, value={}",
                    action.getSensorId(), action.getType(), action.getValue());
        }
    }
}