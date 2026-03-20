package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.aggregator.config.KafkaProperties;
import ru.yandex.practicum.aggregator.model.SnapshotManager;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final SnapshotManager snapshotManager;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final KafkaProperties properties;

    public void processEvent(SensorEventAvro event) {
        snapshotManager.updateState(event)
                .ifPresent(this::sendSnapshot);
    }

    private void sendSnapshot(SensorsSnapshotAvro snapshot) {
        try {
            ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(
                    properties.getTopics().getSnapshots(),
                    snapshot.getHubId(),
                    snapshot
            );

            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка отправки снапшота в Kafka", exception);
                } else {
                    log.debug("Снапшот отправлен: hubId={}, partition={}, offset={}",
                            snapshot.getHubId(), metadata.partition(), metadata.offset());
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при отправке снапшота", e);
        }
    }
}