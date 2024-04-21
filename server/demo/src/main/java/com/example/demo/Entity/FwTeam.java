package com.example.demo.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "fw_team")
public class FwTeam {

    @Id
    @Column(name = "teamId")
    private String id;

    @Column(name = "guidelines")
    private String guidelines;

    public FwTeam() {
        this.id = UUID.randomUUID().toString();
    }
}