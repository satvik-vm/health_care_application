package com.example.demo.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Field_Worker")
public class FieldWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_worker_id")
    private int id;

    @Column(name = "area", nullable = false)
    private String area;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_id", nullable = false)
    private User user;
}
