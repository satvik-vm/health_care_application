package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Data
@Setter
@Table(name = "roles")
public class Role {
    @Id
    @Column(name = "role_id")
    private String id;

    @Column(name = "role_name", nullable = false)
    private String name;

    public Role(){
        this.id = UUID.randomUUID().toString();
    }
}
