package ru.yandex.practicum.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.entity.Condition;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Component
public class ConditionEvaluator {

    public boolean evaluate(Condition condition, SensorStateAvro sensorState) {
        if (sensorState == null) {
            log.debug("Состояние датчика отсутствует для условия: {}", condition.getSensorId());
            return false;
        }

        Object sensorValue = extractValue(sensorState.getData());
        if (sensorValue == null) {
            log.debug("Не удалось извлечь значение из датчика: {}", condition.getSensorId());
            return false;
        }

        Integer expectedValue = condition.getValue();
        String operation = condition.getOperation();

        return compare(sensorValue, expectedValue, operation);
    }

    private Object extractValue(Object data) {
        if (data instanceof ClimateSensorAvro) {
            return ((ClimateSensorAvro) data).getTemperatureC();
        }
        if (data instanceof LightSensorAvro) {
            return ((LightSensorAvro) data).getLuminosity();
        }
        if (data instanceof MotionSensorAvro) {
            return ((MotionSensorAvro) data).getMotion();
        }
        if (data instanceof SwitchSensorAvro) {
            return ((SwitchSensorAvro) data).getState();
        }
        if (data instanceof TemperatureSensorAvro) {
            return ((TemperatureSensorAvro) data).getTemperatureC();
        }
        return null;
    }

    private boolean compare(Object sensorValue, Integer expectedValue, String operation) {
        if (sensorValue instanceof Boolean) {
            boolean boolValue = (boolean) sensorValue;
            boolean expectedBool = expectedValue != null && expectedValue == 1;

            switch (operation) {
                case "EQUALS":
                    return boolValue == expectedBool;
                default:
                    log.warn("Операция {} не поддерживается для булевых значений", operation);
                    return false;
            }
        }

        if (sensorValue instanceof Integer) {
            int intValue = (Integer) sensorValue;
            int expected = expectedValue != null ? expectedValue : 0;

            switch (operation) {
                case "EQUALS":
                    return intValue == expected;
                case "GREATER_THAN":
                    return intValue > expected;
                case "LOWER_THAN":
                    return intValue < expected;
                default:
                    log.warn("Неизвестная операция: {}", operation);
                    return false;
            }
        }

        log.warn("Неизвестный тип значения: {}", sensorValue.getClass().getSimpleName());
        return false;
    }
}