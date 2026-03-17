package ru.yandex.practicum.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.handler.SensorEventHandler;
import ru.yandex.practicum.collector.model.sensor.TemperatureSensorEvent;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureSensorHandler implements SensorEventHandler {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Обработка события от датчика температуры: id={}, hubId={}",
                event.getId(), event.getHubId());

        TemperatureSensorEvent tempEvent = mapToInternal(event);
        kafkaEventProducer.sendSensorEvent(tempEvent);

        log.debug("Событие от датчика температуры успешно обработано");
    }

    private TemperatureSensorEvent mapToInternal(SensorEventProto event) {
        var tempData = event.getTemperatureSensor();
        Instant timestamp = Instant.ofEpochMilli(event.getTimestamp());

        TemperatureSensorEvent tempEvent = new TemperatureSensorEvent();
        tempEvent.setId(event.getId());
        tempEvent.setHubId(event.getHubId());
        tempEvent.setTimestamp(timestamp);
        tempEvent.setTemperatureC(tempData.getTemperatureC());
        tempEvent.setTemperatureF(tempData.getTemperatureF());

        return tempEvent;
    }
}