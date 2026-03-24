package ru.yandex.practicum.analyzer.service;

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
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private final HubEventHandler hubEventHandler;
    private final KafkaProperties properties;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public HubEventProcessor(KafkaConsumer<String, HubEventAvro> hubEventConsumer,
                             HubEventHandler hubEventHandler,
                             KafkaProperties properties) {
        this.hubEventConsumer = hubEventConsumer;
        this.hubEventHandler = hubEventHandler;
        this.properties = properties;

        addShutdownHook();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сработал хук на завершение JVM. Прерываю работу консьюмера событий хаба.");
            closed.set(true);
            hubEventConsumer.wakeup();
        }));
    }

    @Override
    public void run() {
        try {
            hubEventConsumer.subscribe(java.util.List.of(properties.getTopics().getHubEvents()));
            log.info("HubEventProcessor подписан на топик: {}", properties.getTopics().getHubEvents());

            Duration pollTimeout = Duration.ofMillis(properties.getHubEventConsumer().getPollTimeoutMs());

            while (!closed.get()) {
                try {
                    ConsumerRecords<String, HubEventAvro> records = hubEventConsumer.poll(pollTimeout);

                    if (!records.isEmpty()) {
                        for (ConsumerRecord<String, HubEventAvro> record : records) {
                            log.debug("Получено событие хаба: hubId={}, offset={}",
                                    record.value().getHubId(), record.offset());

                            hubEventHandler.handle(record.value());
                        }

                        hubEventConsumer.commitSync();
                    }
                } catch (WakeupException e) {
                    if (!closed.get()) {
                        throw e;
                    }
                }
            }

        } catch (WakeupException e) {
            log.info("Получен сигнал завершения HubEventProcessor");
        } catch (Exception e) {
            log.error("Ошибка при обработке событий хаба", e);
        } finally {
            try {
                hubEventConsumer.commitSync();
            } catch (Exception e) {
                log.error("Ошибка при финальном коммите offset'ов", e);
            } finally {
                log.info("Закрываем HubEventConsumer");
                hubEventConsumer.close();
            }
        }
    }
}