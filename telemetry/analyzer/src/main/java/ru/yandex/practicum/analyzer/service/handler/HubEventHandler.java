package ru.yandex.practicum.analyzer.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.entity.Action;
import ru.yandex.practicum.analyzer.model.entity.Condition;
import ru.yandex.practicum.analyzer.model.entity.Scenario;
import ru.yandex.practicum.analyzer.model.entity.Sensor;
import ru.yandex.practicum.analyzer.model.repository.ActionRepository;
import ru.yandex.practicum.analyzer.model.repository.ConditionRepository;
import ru.yandex.practicum.analyzer.model.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.model.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventHandler {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    public void handle(HubEventAvro event) {
        Object payload = event.getPayload();

        if (payload instanceof DeviceAddedEventAvro) {
            handleDeviceAdded((DeviceAddedEventAvro) payload, event.getHubId());
        } else if (payload instanceof DeviceRemovedEventAvro) {
            handleDeviceRemoved((DeviceRemovedEventAvro) payload);
        } else if (payload instanceof ScenarioAddedEventAvro) {
            handleScenarioAdded((ScenarioAddedEventAvro) payload, event.getHubId());
        } else if (payload instanceof ScenarioRemovedEventAvro) {
            handleScenarioRemoved((ScenarioRemovedEventAvro) payload, event.getHubId());
        } else {
            log.warn("Неизвестный тип события хаба: {}", payload != null ? payload.getClass() : "null");
        }
    }

    private void handleDeviceAdded(DeviceAddedEventAvro device, String hubId) {
        Sensor sensor = new Sensor();
        sensor.setId(device.getId());
        sensor.setHubId(hubId);

        sensorRepository.save(sensor);
        log.info("Добавлен датчик: id={}, hubId={}", device.getId(), hubId);
    }

    private void handleDeviceRemoved(DeviceRemovedEventAvro device) {
        sensorRepository.deleteById(device.getId());
        log.info("Удален датчик: id={}", device.getId());
    }

    private void handleScenarioAdded(ScenarioAddedEventAvro scenarioEvent, String hubId) {
        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(scenarioEvent.getName());

        java.util.List<Condition> conditions = scenarioEvent.getConditions().stream()
                .map(this::toCondition)
                .collect(Collectors.toList());
        conditionRepository.saveAll(conditions);

        java.util.List<Action> actions = scenarioEvent.getActions().stream()
                .map(this::toAction)
                .collect(Collectors.toList());
        actionRepository.saveAll(actions);

        scenario.setConditions(conditions);
        scenario.setActions(actions);

        java.util.List<Sensor> sensors = conditions.stream()
                .map(c -> sensorRepository.findById(c.getSensorId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());
        scenario.setSensors(sensors);

        scenarioRepository.save(scenario);
        log.info("Добавлен сценарий: hubId={}, name={}", hubId, scenarioEvent.getName());
    }

    private void handleScenarioRemoved(ScenarioRemovedEventAvro scenarioEvent, String hubId) {
        scenarioRepository.findByHubIdAndName(hubId, scenarioEvent.getName())
                .ifPresent(scenario -> {
                    scenarioRepository.delete(scenario);
                    log.info("Удален сценарий: hubId={}, name={}", hubId, scenarioEvent.getName());
                });
    }

    private Condition toCondition(ScenarioConditionAvro conditionProto) {
        Condition condition = new Condition();
        condition.setSensorId(conditionProto.getSensorId());
        condition.setType(conditionProto.getType().name());
        condition.setOperation(conditionProto.getOperation().name());

        Object value = conditionProto.getValue();

        if (value instanceof Integer) {
            condition.setValue((Integer) value);
        } else if (value instanceof Boolean) {
            condition.setValue(((Boolean) value) ? 1 : 0);
        } else {
            condition.setValue(null);
        }

        return condition;
    }

    private Action toAction(DeviceActionAvro actionProto) {
        Action action = new Action();
        action.setSensorId(actionProto.getSensorId());
        action.setType(actionProto.getType().name());
        action.setValue(actionProto.getValue() != null ? actionProto.getValue() : null);
        return action;
    }
}