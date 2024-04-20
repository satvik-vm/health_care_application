package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "patient")
public class Patient {

    @Id
    @Column(name = "patient_id")
    private String id;

    @Column(name = "aabhaId", unique = true, nullable = false)
    private String aabhaId;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "icd10_code")
    private String icd10Code;

    @Column(name = "fwAssistance")
    private boolean fwAssistance;

    @Column(name="state")
    private String state;

    @Column(name="subDivision")
    private String subDivision;

    @Column(name="district")
    private String district;

    @Column(name="status")
    private String healthStatus;

    @Column(name="dateOfRegistration")
    private Date dateOfRegistration;

    @Column(name="mostRecentVisit")
    private Date mostRecentVisit;

    @ManyToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="fw_id")
    private FieldWorker fw;

    @OneToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    public Patient(){
        this.id = UUID.randomUUID().toString();
    }
}

