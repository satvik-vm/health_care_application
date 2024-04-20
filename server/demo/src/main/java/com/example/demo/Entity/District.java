package com.example.demo.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="district")
@Getter
@Setter
public class District {
    @Id
    @Column(name="district_id")
    private String disrictId;

    @Column(name = "name", nullable = false)
    private String name;

    public District() {
        this.disrictId = UUID.randomUUID().toString();
    }
}
