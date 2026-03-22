package ru.yandex.practicum.analyzer.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    private String id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;
}