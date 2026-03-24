package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.config.KafkaProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final SnapshotService snapshotService;
    private final KafkaProperties properties;

    public void start() {
        try {
            consumer.subscribe(java.util.List.of(properties.getTopics().getSensors()));
            log.info("Aggregator подписан на топик: {}", properties.getTopics().getSensors());

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.debug("Получено событие: key={}, offset={}, hubId={}, sensorId={}",
                            record.key(), record.offset(),
                            record.value().getHubId(), record.value().getId());

                    snapshotService.processEvent(record.value());
                }

                consumer.commitSync();
            }

        } catch (WakeupException e) {
            log.info("Получен сигнал завершения работы Aggregator");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                log.info("Сбрасываем буфер продюсера...");
                producer.flush();

                log.info("Фиксируем оффсеты консьюмера...");
                consumer.commitSync();

                log.info("Все оффсеты зафиксированы");
            } catch (Exception e) {
                log.error("Ошибка при фиксации оффсетов", e);
            } finally {
                try {
                    log.info("Закрываем консьюмер");
                    consumer.close();
                } catch (Exception e) {
                    log.error("Ошибка при закрытии консьюмера", e);
                }

                try {
                    log.info("Закрываем продюсер");
                    producer.close();
                } catch (Exception e) {
                    log.error("Ошибка при закрытии продюсера", e);
                }
            }
        }
    }
}