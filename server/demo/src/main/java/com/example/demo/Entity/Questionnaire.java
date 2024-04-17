package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "questionnaireSet")
public class Questionnaire {
    @Id
    @Column(name = "qid")
    private String id;

    @Column(name="qn_Name", unique = true)
    private String name;

//    @OneToMany(mappedBy = "qn", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Question> questions;

    public Questionnaire(){
        this.id = UUID.randomUUID().toString();
    }
}
