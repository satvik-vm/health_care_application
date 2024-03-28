package com.example.demo.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="district")
@Getter
@Setter
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="district_id")
    private int disrictId;

    @Column(name = "name", nullable = false)
    private String name;
}
