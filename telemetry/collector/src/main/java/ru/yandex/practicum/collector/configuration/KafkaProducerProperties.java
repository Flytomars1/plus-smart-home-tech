package ru.yandex.practicum.collector.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "collector.kafka")
public class KafkaProducerProperties {

    private final Producer producer = new Producer();
    private final Topics topics = new Topics();

    @Getter
    @Setter
    public static class Producer {
        private String bootstrapServers;
        private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
        private String valueSerializer = "ru.yandex.practicum.collector.service.AvroSerializer";
        private String acks = "all";
        private int retries = 3;
        private boolean enableIdempotence = true;
        private int batchSize = 16384;
        private int lingerMs = 10;
        private int bufferMemory = 33554432;
        private int requestTimeoutMs = 30000;
        private int deliveryTimeoutMs = 120000;
    }

    @Getter
    @Setter
    public static class Topics {
        private String sensors = "telemetry.sensors.v1";
        private String hubs = "telemetry.hubs.v1";
    }
}