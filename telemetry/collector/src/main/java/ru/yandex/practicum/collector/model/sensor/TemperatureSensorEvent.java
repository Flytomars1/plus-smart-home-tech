package ru.yandex.practicum.collector.model.sensor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Getter
@Setter
@ToString(callSuper = true)
public class TemperatureSensorEvent extends SensorEvent {
    private int temperatureC;
    private int temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    public SensorEventAvro toAvro() {
        return SensorEventAvro.newBuilder()
                .setId(getId())
                .setHubId(getHubId())
                .setTimestamp(getTimestamp())
                .setPayload(
                        TemperatureSensorAvro.newBuilder()
                                .setId(getId())
                                .setHubId(getHubId())
                                .setTimestamp(getTimestamp())
                                .setTemperatureC(temperatureC)
                                .setTemperatureF(temperatureF)
                                .build()
                )
                .build();
    }
}