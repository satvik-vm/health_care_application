package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.util.List;

@Entity
@Table(name="supervisor")
@Getter
@Setter
public class Supervisor{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sup_id")
    private int id;

    @Column(name = "district", nullable = false)
    private String district;

    @OneToMany(mappedBy = "supervisor")
    private List<FieldWorker> fieldWorkers;

    @OneToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="user_id", nullable = false)
    private User user;
}