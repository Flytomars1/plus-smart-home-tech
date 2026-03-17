package ru.yandex.practicum.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.handler.SensorEventHandler;
import ru.yandex.practicum.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class LightSensorHandler implements SensorEventHandler {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Обработка события от датчика освещенности: id={}, hubId={}",
                event.getId(), event.getHubId());

        LightSensorEvent lightEvent = mapToInternal(event);
        kafkaEventProducer.sendSensorEvent(lightEvent);

        log.debug("Событие от датчика освещенности успешно обработано");
    }

    private LightSensorEvent mapToInternal(SensorEventProto event) {
        var lightData = event.getLightSensor();
        Instant timestamp = Instant.ofEpochMilli(event.getTimestamp());

        LightSensorEvent lightEvent = new LightSensorEvent();
        lightEvent.setId(event.getId());
        lightEvent.setHubId(event.getHubId());
        lightEvent.setTimestamp(timestamp);
        lightEvent.setLinkQuality(lightData.getLinkQuality());
        lightEvent.setLuminosity(lightData.getLuminosity());

        return lightEvent;
    }
}