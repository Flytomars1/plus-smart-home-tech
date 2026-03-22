package ru.yandex.practicum.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.handler.SensorEventHandler;
import ru.yandex.practicum.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class MotionSensorHandler implements SensorEventHandler {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Обработка события от датчика движения: id={}, hubId={}",
                event.getId(), event.getHubId());

        MotionSensorEvent motionEvent = mapToInternal(event);

        kafkaEventProducer.sendSensorEvent(motionEvent);

        log.debug("Событие от датчика движения успешно обработано");
    }

    private MotionSensorEvent mapToInternal(SensorEventProto event) {
        var motionData = event.getMotionSensor();
        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        MotionSensorEvent motionEvent = new MotionSensorEvent();
        motionEvent.setId(event.getId());
        motionEvent.setHubId(event.getHubId());
        motionEvent.setTimestamp(timestamp);
        motionEvent.setLinkQuality(motionData.getLinkQuality());
        motionEvent.setMotion(motionData.getMotion());
        motionEvent.setVoltage(motionData.getVoltage());

        return motionEvent;
    }
}