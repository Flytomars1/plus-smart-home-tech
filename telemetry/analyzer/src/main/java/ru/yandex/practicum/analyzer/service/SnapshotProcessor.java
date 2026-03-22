package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final ScenarioExecutor scenarioExecutor;
    private final KafkaProperties properties;

    public void start() {
        try {
            snapshotConsumer.subscribe(java.util.List.of(properties.getTopics().getSnapshots()));
            log.info("SnapshotProcessor подписан на топик: {}", properties.getTopics().getSnapshots());

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    log.debug("Получен снапшот: hubId={}, offset={}",
                            record.value().getHubId(), record.offset());

                    scenarioExecutor.executeScenarios(record.value());
                }

                snapshotConsumer.commitSync();
            }

        } catch (WakeupException e) {
            log.info("Получен сигнал завершения SnapshotProcessor");
        } catch (Exception e) {
            log.error("Ошибка при обработке снапшотов", e);
        } finally {
            try {
                snapshotConsumer.commitSync();
            } finally {
                log.info("Закрываем SnapshotConsumer");
                snapshotConsumer.close();
            }
        }
    }
}