package ru.yandex.practicum.collector.configuration;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    private final KafkaProducerProperties properties;

    public KafkaConfig(KafkaProducerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public KafkaProducer<String, Object> kafkaProducer() {
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getProducer().getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                properties.getProducer().getKeySerializer());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                properties.getProducer().getValueSerializer());

        props.put(ProducerConfig.ACKS_CONFIG,
                properties.getProducer().getAcks());
        props.put(ProducerConfig.RETRIES_CONFIG,
                properties.getProducer().getRetries());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                properties.getProducer().isEnableIdempotence());

        props.put(ProducerConfig.BATCH_SIZE_CONFIG,
                properties.getProducer().getBatchSize());
        props.put(ProducerConfig.LINGER_MS_CONFIG,
                properties.getProducer().getLingerMs());
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,
                properties.getProducer().getBufferMemory());

        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                properties.getProducer().getRequestTimeoutMs());
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
                properties.getProducer().getDeliveryTimeoutMs());

        return new KafkaProducer<>(props);
    }
}