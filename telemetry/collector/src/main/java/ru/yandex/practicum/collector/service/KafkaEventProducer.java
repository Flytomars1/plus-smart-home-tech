package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.sensors}")
    private String sensorsTopic;

    @Value("${kafka.topics.hubs}")
    private String hubsTopic;

    public void sendSensorEvent(SensorEvent event) {
        try {
            SensorEventAvro avroEvent = AvroMapper.toSensorEventAvro(event);
            log.debug("Отправка события датчика в топик {}: {}", sensorsTopic, avroEvent);
            kafkaTemplate.send(sensorsTopic, avroEvent.getHubId(), avroEvent);
        } catch (Exception e) {
            log.error("Ошибка при отправке события датчика в Kafka", e);
            throw new RuntimeException("Ошибка отправки в Kafka", e);
        }
    }

    public void sendHubEvent(HubEvent event) {
        try {
            HubEventAvro avroEvent = AvroMapper.toHubEventAvro(event);
            log.debug("Отправка события хаба в топик {}: {}", hubsTopic, avroEvent);
            kafkaTemplate.send(hubsTopic, avroEvent.getHubId(), avroEvent);
        } catch (Exception e) {
            log.error("Ошибка при отправке события хаба в Kafka", e);
            throw new RuntimeException("Ошибка отправки в Kafka", e);
        }
    }
}