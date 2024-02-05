package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name="supervisor")
@Getter
public class Supervisor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "date_of_joining")
    private String dateOfJoining;

    @Column(name = "district")
    private String district;
}
