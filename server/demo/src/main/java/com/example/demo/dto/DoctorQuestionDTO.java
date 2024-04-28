package com.example.demo.dto;

import lombok.Data;

@Data
public class DoctorQuestionDTO {
    String type;
    String question;
    String optA;
    String optB;
    String optC;
    String optD;
}
