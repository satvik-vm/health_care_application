package com.example.demo.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "hospital")
public class Hospital {
    @Id
    @Column(name = "hospital_id")
    private String id;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "district", nullable = false)
    private String district;

    @Column(name = "subDivision", nullable = false)
    private String subDivision;

    @Column(name="HospitalName")
    private String name;

    @OneToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    public Hospital(){
        this.id = UUID.randomUUID().toString();
    }
}