package com.example.demo.dto;

import lombok.Data;

@Data
public class QuestionDTO {
    int publicId;
    private String type;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
}