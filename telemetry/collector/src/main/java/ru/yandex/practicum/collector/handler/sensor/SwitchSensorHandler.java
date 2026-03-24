package ru.yandex.practicum.collector.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.handler.SensorEventHandler;
import ru.yandex.practicum.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.collector.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class SwitchSensorHandler implements SensorEventHandler {

    private final KafkaEventProducer kafkaEventProducer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Обработка события от датчика-переключателя: id={}, hubId={}",
                event.getId(), event.getHubId());

        SwitchSensorEvent switchEvent = mapToInternal(event);
        kafkaEventProducer.sendSensorEvent(switchEvent);

        log.debug("Событие от датчика-переключателя успешно обработано");
    }

    private SwitchSensorEvent mapToInternal(SensorEventProto event) {
        var switchData = event.getSwitchSensor();

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        SwitchSensorEvent switchEvent = new SwitchSensorEvent();
        switchEvent.setId(event.getId());
        switchEvent.setHubId(event.getHubId());
        switchEvent.setTimestamp(timestamp);
        switchEvent.setState(switchData.getState());

        return switchEvent;
    }
}