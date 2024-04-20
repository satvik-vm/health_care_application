package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="admin")
@Getter
@Setter
public class Admin {
    @Id
    @Column(name="admin_id")
    private String uniqueId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_id")
    private User user;

    public Admin(){
        this.uniqueId = UUID.randomUUID().toString();
    }
}