package ru.yandex.practicum.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private String bootstrapServers = "localhost:9092";

    private final Consumer snapshotConsumer = new Consumer();
    private final Consumer hubEventConsumer = new Consumer();
    private final Topics topics = new Topics();

    @Getter
    @Setter
    public static class Consumer {
        private String groupId;
        private String autoOffsetReset = "earliest";
        private boolean enableAutoCommit = false;
    }

    @Getter
    @Setter
    public static class Topics {
        private String snapshots = "telemetry.snapshots.v1";
        private String hubEvents = "telemetry.hubs.v1";
    }
}