package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "questionnaireSet")
public class Questionnaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qid")
    private int id;

    @Column(name="qn_Name", unique = true)
    private String name;

    @OneToMany(mappedBy = "qn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
}
