package ru.yandex.practicum.aggregator.service.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class SnapshotSerializer implements Serializer<SpecificRecord> {

    @Override
    public byte[] serialize(String topic, SpecificRecord data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            DatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(data.getSchema());

            writer.write(data, encoder);
            encoder.flush();

            byte[] bytes = out.toByteArray();
            log.debug("Сериализован объект {} в {} байт", data.getClass().getSimpleName(), bytes.length);

            return bytes;
        } catch (IOException e) {
            log.error("Ошибка сериализации Avro сообщения", e);
            throw new SerializationException("Ошибка сериализации Avro", e);
        }
    }
}