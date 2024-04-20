package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "question")
public class Question {

    @Id
    @Column(name = "qid")
    private String id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "optionA")
    private String optionA;

    @Column(name = "optionB")
    private String optionB;

    @Column(name = "optionC")
    private String optionC;

    @Column(name = "optionD")
    private String optionD;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "qn_id", nullable = false)
    private Questionnaire qn;

    public Question(){
        this.id = UUID.randomUUID().toString();
    }
}
