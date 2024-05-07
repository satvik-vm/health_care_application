package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "patient_update")
public class UpdateForPatient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int pid;
    private String aabhaId;
    private String firstName;
    private String lastName;
    private String status;
    private String district;
    private String area;
    private String Date;
    private String Time;
    private Boolean isRead;
    private String sender;
    private String receiver;
    private String message;
}
