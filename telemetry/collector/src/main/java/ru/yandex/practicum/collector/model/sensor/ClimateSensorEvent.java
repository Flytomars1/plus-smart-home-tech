package ru.yandex.practicum.collector.model.sensor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Getter
@Setter
@ToString(callSuper = true)
public class ClimateSensorEvent extends SensorEvent {
    private int temperatureC;
    private int humidity;
    private int co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    @Override
    public SensorEventAvro toAvro() {
        return SensorEventAvro.newBuilder()
                .setId(getId())
                .setHubId(getHubId())
                .setTimestamp(getTimestamp())
                .setPayload(
                        ClimateSensorAvro.newBuilder()
                                .setTemperatureC(temperatureC)
                                .setHumidity(humidity)
                                .setCo2Level(co2Level)
                                .build()
                )
                .build();
    }
}