package com.example.demo.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "MedicalRecords")
public class MedicalRecord {
    @Id
    @Column(name="record_id")
    private String uniqueId;

    private String record;

    @ManyToOne
    @JoinColumn(name="patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name="doctor_id")
    private Doctor doctor;

    public MedicalRecord(){
        this.uniqueId = UUID.randomUUID().toString();
    }
}
