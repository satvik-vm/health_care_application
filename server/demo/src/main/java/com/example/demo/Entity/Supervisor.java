package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.util.List;
import java.util.UUID;

@Entity
@Table(name="supervisor")
@Getter
@Setter
public class Supervisor{
    @Id
    @Column(name = "sup_id")
    private String id;

    @Column(name = "state")
    private String state;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    @OneToOne(cascade = {CascadeType.ALL, CascadeType.REMOVE})
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    public Supervisor() {
        this.id = UUID.randomUUID().toString();
    }
}