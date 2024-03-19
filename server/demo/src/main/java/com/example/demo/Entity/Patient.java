package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long id;

    @Column(name = "aabhaId", unique = true, nullable = false)
    private String aabhaId;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "icd10_code")
    private String icd10Code;

    @Column(name = "fwAssistance")
    private boolean fwAssistance;

    @OneToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="user_id", nullable = false)
    private User user;
}

