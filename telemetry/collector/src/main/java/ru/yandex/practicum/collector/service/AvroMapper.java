package ru.yandex.practicum.collector.service;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.collector.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@UtilityClass
public class AvroMapper {

    public SensorEventAvro toSensorEventAvro(SensorEvent event) {
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        if (event instanceof LightSensorEvent) {
            LightSensorEvent e = (LightSensorEvent) event;
            builder.setPayload(
                    LightSensorAvro.newBuilder()
                            .setLinkQuality(e.getLinkQuality())
                            .setLuminosity(e.getLuminosity())
                            .build()
            );
        } else if (event instanceof TemperatureSensorEvent) {
            TemperatureSensorEvent e = (TemperatureSensorEvent) event;
            builder.setPayload(
                    TemperatureSensorAvro.newBuilder()
                            .setId(e.getId())
                            .setHubId(e.getHubId())
                            .setTimestamp(e.getTimestamp())
                            .setTemperatureC(e.getTemperatureC())
                            .setTemperatureF(e.getTemperatureF())
                            .build()
            );
        } else if (event instanceof MotionSensorEvent) {
            MotionSensorEvent e = (MotionSensorEvent) event;
            builder.setPayload(
                    MotionSensorAvro.newBuilder()
                            .setLinkQuality(e.getLinkQuality())
                            .setMotion(e.isMotion())
                            .setVoltage(e.getVoltage())
                            .build()
            );
        } else if (event instanceof ClimateSensorEvent) {
            ClimateSensorEvent e = (ClimateSensorEvent) event;
            builder.setPayload(
                    ClimateSensorAvro.newBuilder()
                            .setTemperatureC(e.getTemperatureC())
                            .setHumidity(e.getHumidity())
                            .setCo2Level(e.getCo2Level())
                            .build()
            );
        } else if (event instanceof SwitchSensorEvent) {
            SwitchSensorEvent e = (SwitchSensorEvent) event;
            builder.setPayload(
                    SwitchSensorAvro.newBuilder()
                            .setState(e.isState())
                            .build()
            );
        }

        return builder.build();
    }

    public HubEventAvro toHubEventAvro(HubEvent event) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        if (event instanceof DeviceAddedEvent) {
            DeviceAddedEvent e = (DeviceAddedEvent) event;
            builder.setPayload(
                    DeviceAddedEventAvro.newBuilder()
                            .setId(e.getId())
                            .setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()))
                            .build()
            );
        } else if (event instanceof DeviceRemovedEvent) {
            DeviceRemovedEvent e = (DeviceRemovedEvent) event;
            builder.setPayload(
                    DeviceRemovedEventAvro.newBuilder()
                            .setId(e.getId())
                            .build()
            );
        } else if (event instanceof ScenarioAddedEvent) {
            ScenarioAddedEvent e = (ScenarioAddedEvent) event;
            builder.setPayload(
                    ScenarioAddedEventAvro.newBuilder()
                            .setName(e.getName())
                            .setConditions(e.getConditions().stream()
                                    .map(AvroMapper::toScenarioConditionAvro)
                                    .toList())
                            .setActions(e.getActions().stream()
                                    .map(AvroMapper::toDeviceActionAvro)
                                    .toList())
                            .build()
            );
        } else if (event instanceof ScenarioRemovedEvent) {
            ScenarioRemovedEvent e = (ScenarioRemovedEvent) event;
            builder.setPayload(
                    ScenarioRemovedEventAvro.newBuilder()
                            .setName(e.getName())
                            .build()
            );
        }

        return builder.build();
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