package ru.yandex.practicum.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.handler.SensorEventHandler;
import ru.yandex.practicum.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClimateSensorHandler implements SensorEventHandler {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Обработка события от климатического датчика: id={}, hubId={}",
                event.getId(), event.getHubId());

        ClimateSensorEvent climateEvent = mapToInternal(event);
        kafkaEventProducer.sendSensorEvent(climateEvent);

        log.debug("Событие от климатического датчика успешно обработано");
    }

    private ClimateSensorEvent mapToInternal(SensorEventProto event) {
        var climateData = event.getClimateSensor();
        Instant timestamp = Instant.ofEpochMilli(event.getTimestamp());

        ClimateSensorEvent climateEvent = new ClimateSensorEvent();
        climateEvent.setId(event.getId());
        climateEvent.setHubId(event.getHubId());
        climateEvent.setTimestamp(timestamp);
        climateEvent.setTemperatureC(climateData.getTemperatureC());
        climateEvent.setHumidity(climateData.getHumidity());
        climateEvent.setCo2Level(climateData.getCo2Level());

        return climateEvent;
    }
}