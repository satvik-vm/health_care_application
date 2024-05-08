package com.example.demo.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "Field_Worker")
public class FieldWorker {

    @Id
    @Column(name = "field_worker_id")
    private String id;

    @Column(name = "area", nullable = false)
    private String area;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "task_completed")
    private int taskCompleted = 0;
    @Column(name = "task_missed")
    private int taskMissed = 0;
    @Column(name = "task_assigned")
    private int taskAssigned = 0;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @ManyToOne
    @JoinColumn(name = "team")
    private FwTeam team;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    public FieldWorker() {
        this.id = UUID.randomUUID().toString();
    }
}
