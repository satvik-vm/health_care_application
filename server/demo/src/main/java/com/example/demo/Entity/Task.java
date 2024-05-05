package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "entity")
public class Task {
    @Id
    @Column(name = "task_id")
    private String id;

    @Column(name = "description")
    private String description;

    @Column(name = "date")
    private String date;

    @Column(name = "time")
    private String time;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "assigned_time")
    private LocalDateTime assignedTime;

    @Column(name = "duration")
    private Duration duration;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "task_type")
    private String task_type;

    @ManyToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="patient_id")
    private Patient patient;

    @ManyToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="fw_id")
    private FieldWorker fieldWorker;

    @ManyToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="doctor_id")
    private Doctor doctor;

    public Task(){
        this.id = UUID.randomUUID().toString();
    }
}
