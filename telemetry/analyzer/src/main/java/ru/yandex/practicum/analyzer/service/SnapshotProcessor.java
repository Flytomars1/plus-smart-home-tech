package ru.yandex.practicum.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class SnapshotProcessor {

    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final ScenarioExecutor scenarioExecutor;
    private final KafkaProperties properties;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    private final int commitBatchSize;

    public SnapshotProcessor(KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer,
                             ScenarioExecutor scenarioExecutor,
                             KafkaProperties properties) {
        this.snapshotConsumer = snapshotConsumer;
        this.scenarioExecutor = scenarioExecutor;
        this.properties = properties;
        this.commitBatchSize = properties.getSnapshotConsumer().getCommitBatchSize();

        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сработал хук на завершение JVM. Прерываю работу консьюмера снапшотов.");
            closed.set(true);
            snapshotConsumer.wakeup();
        }));
    }

    public void start() {
        try {
            snapshotConsumer.subscribe(java.util.List.of(properties.getTopics().getSnapshots()));
            log.info("SnapshotProcessor подписан на топик: {}", properties.getTopics().getSnapshots());

            Duration pollTimeout = Duration.ofMillis(properties.getSnapshotConsumer().getPollTimeoutMs());

            while (!closed.get()) {
                try {
                    ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(pollTimeout);

                    if (!records.isEmpty()) {
                        int processedCount = 0;

                        for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                            log.debug("Получен снапшот: hubId={}, offset={}",
                                    record.value().getHubId(), record.offset());

                            scenarioExecutor.executeScenarios(record.value());

                            manageOffsets(record, ++processedCount);
                        }

                        if (!currentOffsets.isEmpty()) {
                            commitRemainingOffsets();
                        }
                    }
                } catch (WakeupException e) {
                    if (!closed.get()) {
                        throw e;
                    }
                }
            }

        } catch (WakeupException e) {
            log.info("Получен сигнал завершения SnapshotProcessor");
        } catch (Exception e) {
            log.error("Ошибка при обработке снапшотов", e);
        } finally {
            try {
                if (!currentOffsets.isEmpty()) {
                    log.info("Финальный коммит offset'ов: {}", currentOffsets);
                    snapshotConsumer.commitSync(currentOffsets);
                }
            } catch (Exception e) {
                log.error("Ошибка при финальном коммите offset'ов", e);
            } finally {
                log.info("Закрываем SnapshotConsumer");
                snapshotConsumer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record, int count) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % commitBatchSize == 0) {
            commitCurrentOffsetsAsync();
        }
    }

    private void commitCurrentOffsetsAsync() {
        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>(currentOffsets);

        snapshotConsumer.commitAsync(offsetsToCommit, (offsets, exception) -> {
            if (exception != null) {
                log.warn("Ошибка во время асинхронной фиксации offset'ов: {}", offsets, exception);
            } else {
                log.debug("Асинхронно зафиксированы offset'ы: {}", offsets);
                currentOffsets.keySet().removeAll(offsets.keySet());
            }
        });
    }

    private void commitRemainingOffsets() {
        try {
            log.debug("Синхронный коммит оставшихся offset'ов: {}", currentOffsets);
            snapshotConsumer.commitSync(currentOffsets);
            currentOffsets.clear();
        } catch (Exception e) {
            log.error("Ошибка при синхронном коммите offset'ов", e);
        }
    }
}