package ru.yandex.practicum.collector.model.sensor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Getter
@Setter
@ToString(callSuper = true)
public class LightSensorEvent extends SensorEvent {
    private int linkQuality;
    private int luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    public SensorEventAvro toAvro() {
        return SensorEventAvro.newBuilder()
                .setId(getId())
                .setHubId(getHubId())
                .setTimestamp(getTimestamp())
                .setPayload(
                        LightSensorAvro.newBuilder()
                                .setLinkQuality(linkQuality)
                                .setLuminosity(luminosity)
                                .build()
                )
                .build();
    }
}