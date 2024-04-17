package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="id_mapping")
@Getter
@Setter
public class IdMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "public_id")
    private int publicId;

    @Column(name = "private_id")
    private UUID privateId;
}
