package ru.yandex.practicum.collector.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
// Импортируем наши модели с понятными именами
import ru.yandex.practicum.collector.model.sensor.*;
import ru.yandex.practicum.collector.model.hub.*;
// Protobuf классы оставляем с полными именами в коде или используем алиасы
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorResponseProto;
import ru.yandex.practicum.grpc.telemetry.event.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public void collectSensorEvent(
            ru.yandex.practicum.grpc.telemetry.event.SensorEventProto request,  // Явно указываем полный путь
            StreamObserver<CollectorResponseProto> responseObserver) {

        log.info("Получено gRPC событие от датчика: id={}, hubId={}",
                request.getId(), request.getHubId());

        try {
            // Преобразуем Protobuf в нашу модель
            ru.yandex.practicum.collector.model.sensor.SensorEvent sensorEvent = mapToSensorEvent(request);

            // Отправляем в Kafka
            kafkaEventProducer.sendSensorEvent(sensorEvent);

            CollectorResponseProto response = CollectorResponseProto.newBuilder()
                    .setSuccess(true)
                    .setMessage("Событие датчика успешно обработано")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка при обработке gRPC события датчика", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(
            ru.yandex.practicum.grpc.telemetry.event.HubEventProto request,  // Явно указываем полный путь
            StreamObserver<CollectorResponseProto> responseObserver) {

        log.info("Получено gRPC событие от хаба: hubId={}", request.getHubId());

        try {
            // Преобразуем Protobuf в нашу модель
            ru.yandex.practicum.collector.model.hub.HubEvent hubEvent = mapToHubEvent(request);

            // Отправляем в Kafka
            kafkaEventProducer.sendHubEvent(hubEvent);

            CollectorResponseProto response = CollectorResponseProto.newBuilder()
                    .setSuccess(true)
                    .setMessage("Событие хаба успешно обработано")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка при обработке gRPC события хаба", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e)
            ));
        }
    }

    // В сигнатурах методов тоже используем полные имена Protobuf классов
    private ru.yandex.practicum.collector.model.sensor.SensorEvent mapToSensorEvent(
            ru.yandex.practicum.grpc.telemetry.event.SensorEventProto proto) {

        Instant timestamp = Instant.ofEpochMilli(proto.getTimestamp());

        switch (proto.getPayloadCase()) {
            case MOTION_SENSOR:
                ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto motion = proto.getMotionSensor();
                MotionSensorEvent motionEvent = new MotionSensorEvent();
                motionEvent.setId(proto.getId());
                motionEvent.setHubId(proto.getHubId());
                motionEvent.setTimestamp(timestamp);
                motionEvent.setLinkQuality(motion.getLinkQuality());
                motionEvent.setMotion(motion.getMotion());
                motionEvent.setVoltage(motion.getVoltage());
                return motionEvent;

            case TEMPERATURE_SENSOR:
                ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto temp = proto.getTemperatureSensor();
                TemperatureSensorEvent tempEvent = new TemperatureSensorEvent();
                tempEvent.setId(proto.getId());
                tempEvent.setHubId(proto.getHubId());
                tempEvent.setTimestamp(timestamp);
                tempEvent.setTemperatureC(temp.getTemperatureC());
                tempEvent.setTemperatureF(temp.getTemperatureF());
                return tempEvent;

            case LIGHT_SENSOR:
                ru.yandex.practicum.grpc.telemetry.event.LightSensorProto light = proto.getLightSensor();
                LightSensorEvent lightEvent = new LightSensorEvent();
                lightEvent.setId(proto.getId());
                lightEvent.setHubId(proto.getHubId());
                lightEvent.setTimestamp(timestamp);
                lightEvent.setLinkQuality(light.getLinkQuality());
                lightEvent.setLuminosity(light.getLuminosity());
                return lightEvent;

            case CLIMATE_SENSOR:
                ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto climate = proto.getClimateSensor();
                ClimateSensorEvent climateEvent = new ClimateSensorEvent();
                climateEvent.setId(proto.getId());
                climateEvent.setHubId(proto.getHubId());
                climateEvent.setTimestamp(timestamp);
                climateEvent.setTemperatureC(climate.getTemperatureC());
                climateEvent.setHumidity(climate.getHumidity());
                climateEvent.setCo2Level(climate.getCo2Level());
                return climateEvent;

            case SWITCH_SENSOR:
                ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto switchSensor = proto.getSwitchSensor();
                SwitchSensorEvent switchEvent = new SwitchSensorEvent();
                switchEvent.setId(proto.getId());
                switchEvent.setHubId(proto.getHubId());
                switchEvent.setTimestamp(timestamp);
                switchEvent.setState(switchSensor.getState());
                return switchEvent;

            default:
                throw new IllegalArgumentException("Неизвестный тип сенсора: " + proto.getPayloadCase());
        }
    }

    private ru.yandex.practicum.collector.model.hub.HubEvent mapToHubEvent(
            ru.yandex.practicum.grpc.telemetry.event.HubEventProto proto) {

        Instant timestamp = Instant.ofEpochMilli(proto.getTimestamp());

        switch (proto.getPayloadCase()) {
            case DEVICE_ADDED:
                ru.yandex.practicum.grpc.telemetry.event.DeviceAddedEventProto deviceAdded = proto.getDeviceAdded();
                DeviceAddedEvent addedEvent = new DeviceAddedEvent();
                addedEvent.setHubId(proto.getHubId());
                addedEvent.setTimestamp(timestamp);
                addedEvent.setId(deviceAdded.getId());
                addedEvent.setDeviceType(mapDeviceType(deviceAdded.getType()));
                return addedEvent;

            case DEVICE_REMOVED:
                ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto deviceRemoved = proto.getDeviceRemoved();
                DeviceRemovedEvent removedEvent = new DeviceRemovedEvent();
                removedEvent.setHubId(proto.getHubId());
                removedEvent.setTimestamp(timestamp);
                removedEvent.setId(deviceRemoved.getId());
                return removedEvent;

            case SCENARIO_ADDED:
                ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto scenarioAdded = proto.getScenarioAdded();
                ScenarioAddedEvent addedScenario = new ScenarioAddedEvent();
                addedScenario.setHubId(proto.getHubId());
                addedScenario.setTimestamp(timestamp);
                addedScenario.setName(scenarioAdded.getName());
                addedScenario.setConditions(mapConditions(scenarioAdded.getConditionsList()));
                addedScenario.setActions(mapActions(scenarioAdded.getActionsList()));
                return addedScenario;

            case SCENARIO_REMOVED:
                ru.yandex.practicum.grpc.telemetry.event.ScenarioRemovedEventProto scenarioRemoved = proto.getScenarioRemoved();
                ScenarioRemovedEvent removedScenario = new ScenarioRemovedEvent();
                removedScenario.setHubId(proto.getHubId());
                removedScenario.setTimestamp(timestamp);
                removedScenario.setName(scenarioRemoved.getName());
                return removedScenario;

            default:
                throw new IllegalArgumentException("Неизвестный тип события хаба: " + proto.getPayloadCase());
        }
    }

    // Остальные методы остаются без изменений
    private List<ScenarioCondition> mapConditions(List<ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto> protos) {
        List<ScenarioCondition> conditions = new ArrayList<>();
        for (ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto proto : protos) {
            conditions.add(mapCondition(proto));
        }
        return conditions;
    }

    private ScenarioCondition mapCondition(ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto proto) {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId(proto.getSensorId());
        condition.setType(mapConditionType(proto.getType()));
        condition.setOperation(mapConditionOperation(proto.getOperation()));

        switch (proto.getValueCase()) {
            case INT_VALUE:
                condition.setValue(proto.getIntValue());
                break;
            case BOOL_VALUE:
                condition.setValue(proto.getBoolValue());
                break;
            default:
                condition.setValue(null);
        }

        return condition;
    }

    private List<DeviceAction> mapActions(List<ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto> protos) {
        List<DeviceAction> actions = new ArrayList<>();
        for (ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto proto : protos) {
            actions.add(mapAction(proto));
        }
        return actions;
    }

    private DeviceAction mapAction(ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto proto) {
        DeviceAction action = new DeviceAction();
        action.setSensorId(proto.getSensorId());
        action.setType(mapActionType(proto.getType()));
        action.setValue(proto.hasValue() ? proto.getValue() : null);
        return action;
    }

    // Enum мапперы остаются без изменений
    private DeviceType mapDeviceType(ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto proto) {
        return switch (proto) {
            case MOTION_SENSOR -> DeviceType.MOTION_SENSOR;
            case TEMPERATURE_SENSOR -> DeviceType.TEMPERATURE_SENSOR;
            case LIGHT_SENSOR -> DeviceType.LIGHT_SENSOR;
            case CLIMATE_SENSOR -> DeviceType.CLIMATE_SENSOR;
            case SWITCH_SENSOR -> DeviceType.SWITCH_SENSOR;
            default -> throw new IllegalArgumentException("Неизвестный тип устройства: " + proto);
        };
    }

    private ConditionType mapConditionType(ru.yandex.practicum.grpc.telemetry.event.ConditionTypeProto proto) {
        return switch (proto) {
            case MOTION -> ConditionType.MOTION;
            case LUMINOSITY -> ConditionType.LUMINOSITY;
            case SWITCH -> ConditionType.SWITCH;
            case TEMPERATURE -> ConditionType.TEMPERATURE;
            case CO2LEVEL -> ConditionType.CO2LEVEL;
            case HUMIDITY -> ConditionType.HUMIDITY;
            default -> throw new IllegalArgumentException("Неизвестный тип условия: " + proto);
        };
    }

    private ConditionOperation mapConditionOperation(ru.yandex.practicum.grpc.telemetry.event.ConditionOperationProto proto) {
        return switch (proto) {
            case EQUALS -> ConditionOperation.EQUALS;
            case GREATER_THAN -> ConditionOperation.GREATER_THAN;
            case LOWER_THAN -> ConditionOperation.LOWER_THAN;
            default -> throw new IllegalArgumentException("Неизвестная операция: " + proto);
        };
    }

    private ActionType mapActionType(ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto proto) {
        return switch (proto) {
            case ACTIVATE -> ActionType.ACTIVATE;
            case DEACTIVATE -> ActionType.DEACTIVATE;
            case INVERSE -> ActionType.INVERSE;
            case SET_VALUE -> ActionType.SET_VALUE;
            default -> throw new IllegalArgumentException("Неизвестный тип действия: " + proto);
        };
    }
}