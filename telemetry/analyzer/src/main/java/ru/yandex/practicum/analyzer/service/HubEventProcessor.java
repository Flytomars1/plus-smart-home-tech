package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.config.KafkaProperties;
import ru.yandex.practicum.analyzer.service.handler.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private final HubEventHandler hubEventHandler;
    private final KafkaProperties properties;

    @Override
    public void run() {
        try {
            hubEventConsumer.subscribe(java.util.List.of(properties.getTopics().getHubEvents()));
            log.info("HubEventProcessor подписан на топик: {}", properties.getTopics().getHubEvents());

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = hubEventConsumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    log.debug("Получено событие хаба: hubId={}, offset={}",
                            record.value().getHubId(), record.offset());

                    hubEventHandler.handle(record.value());
                }

                hubEventConsumer.commitSync();
            }

        } catch (WakeupException e) {
            log.info("Получен сигнал завершения HubEventProcessor");
        } catch (Exception e) {
            log.error("Ошибка при обработке событий хаба", e);
        } finally {
            try {
                hubEventConsumer.commitSync();
            } finally {
                log.info("Закрываем HubEventConsumer");
                hubEventConsumer.close();
            }
        }
    }
}