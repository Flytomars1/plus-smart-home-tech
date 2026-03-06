package ru.yandex.practicum.collector.model.sensor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

@Getter
@Setter
@ToString(callSuper = true)
public class SwitchSensorEvent extends SensorEvent {
    private boolean state;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    @Override
    public SensorEventAvro toAvro() {
        return SensorEventAvro.newBuilder()
                .setId(getId())
                .setHubId(getHubId())
                .setTimestamp(getTimestamp())
                .setPayload(
                        SwitchSensorAvro.newBuilder()
                                .setState(state)
                                .build()
                )
                .build();
    }
}