package ru.yandex.practicum.analyzer.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_id", nullable = false)
    private String sensorId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "value")
    private Integer value;
}