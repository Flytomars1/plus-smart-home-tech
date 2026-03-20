package ru.yandex.practicum.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private String bootstrapServers = "localhost:9092";

    private final Consumer consumer = new Consumer();
    private final Producer producer = new Producer();
    private final Topics topics = new Topics();

    @Getter
    @Setter
    public static class Consumer {
        private String groupId = "aggregator";
        private String autoOffsetReset = "earliest";
    }

    @Getter
    @Setter
    public static class Producer {
        private String valueSerializer = "ru.yandex.practicum.aggregator.service.serializer.SnapshotSerializer";
        private String acks = "all";
        private int retries = 3;
    }

    @Getter
    @Setter
    public static class Topics {
        private String sensors = "telemetry.sensors.v1";
        private String snapshots = "telemetry.snapshots.v1";
    }
}