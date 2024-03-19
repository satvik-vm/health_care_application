package com.example.demo.models;

import lombok.Data;

@Data
public class QuestionRequest {
    String type;
    String question;
    String optA;
    String optB;
    String optC;
    String optD;
}
