package com.example.demo.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.print.Doc;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "doctor")
public class Doctor {
    @Id
    @Column(name = "doctor_id")
    private String id;

    @Column(name="registerId")
    private String regId;

    @Column(name = "speciality")
    private String speciality;

    @Column(name="status")
    private String status;

    @ManyToOne
    @JoinColumn(name="hospital_id", nullable = false)
    private Hospital hospital;

    @OneToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    public Doctor(){
        this.id = UUID.randomUUID().toString();
    }
}