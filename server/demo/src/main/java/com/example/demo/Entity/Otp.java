package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "otp")
public class Otp {
    @Id
    @Column(name = "otp_id")
    private String id;

    @Column(name = "otp_num", nullable = false)
    private String otp_num;

    @Column(name = "expDate", nullable = false)
    private Date expDate;

    public Otp(){
        this.id = UUID.randomUUID().toString();
    }
}
