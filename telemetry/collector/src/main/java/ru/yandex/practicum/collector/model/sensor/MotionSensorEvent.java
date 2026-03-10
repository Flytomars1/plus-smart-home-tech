package ru.yandex.practicum.collector.model.sensor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Getter
@Setter
@ToString(callSuper = true)
public class MotionSensorEvent extends SensorEvent {
    private int linkQuality;
    private boolean motion;
    private int voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    public SensorEventAvro toAvro() {
        return SensorEventAvro.newBuilder()
                .setId(getId())
                .setHubId(getHubId())
                .setTimestamp(getTimestamp())
                .setPayload(
                        MotionSensorAvro.newBuilder()
                                .setLinkQuality(linkQuality)
                                .setMotion(motion)
                                .setVoltage(voltage)
                                .build()
                )
                .build();
    }
}