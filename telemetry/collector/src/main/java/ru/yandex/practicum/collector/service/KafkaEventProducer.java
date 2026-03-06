package ru.yandex.practicum.collector.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.configuration.KafkaProducerProperties;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import jakarta.annotation.PreDestroy;

@Slf4j
@Service
@EnableConfigurationProperties(KafkaProducerProperties.class)
public class KafkaEventProducer {

    private final KafkaProducer<String, Object> producer;
    private final KafkaProducerProperties properties;

    public KafkaEventProducer(
            KafkaProducer<String, Object> producer,
            KafkaProducerProperties properties) {
        this.producer = producer;
        this.properties = properties;
    }

    public void sendSensorEvent(SensorEvent event) {
        try {
            SensorEventAvro avroEvent = AvroMapper.toSensorEventAvro(event);
            log.debug("Отправка события датчика в топик {}: {}",
                    properties.getTopics().getSensors(), avroEvent);

            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    properties.getTopics().getSensors(),
                    avroEvent.getHubId(),
                    avroEvent
            );

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка при отправке события датчика в Kafka", exception);
                } else {
                    log.debug("Событие датчика отправлено: partition={}, offset={}",
                            metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при отправке события датчика в Kafka", e);
            throw new RuntimeException("Ошибка отправки в Kafka", e);
        }
    }

    public void sendHubEvent(HubEvent event) {
        try {
            HubEventAvro avroEvent = AvroMapper.toHubEventAvro(event);
            log.debug("Отправка события хаба в топик {}: {}",
                    properties.getTopics().getHubs(), avroEvent);

            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    properties.getTopics().getHubs(),
                    avroEvent.getHubId(),
                    avroEvent
            );

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка при отправке события хаба в Kafka", exception);
                } else {
                    log.debug("Событие хаба отправлено: partition={}, offset={}",
                            metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при отправке события хаба в Kafka", e);
            throw new RuntimeException("Ошибка отправки в Kafka", e);
        }
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close();
            log.info("Kafka producer closed");
        }
    }
}