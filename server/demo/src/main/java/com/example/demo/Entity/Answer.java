package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "answer")
public class Answer {

    @Id
    private String ansId;

    private String mcqAns;

    @Lob
    private String subjAns;

    private int rangeAns;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "qid", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "pid", nullable = false)
    private Patient patient;

    public Answer(){
        this.ansId = UUID.randomUUID().toString();
    }
}
