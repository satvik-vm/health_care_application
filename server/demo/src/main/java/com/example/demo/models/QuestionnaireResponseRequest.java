package com.example.demo.models;

import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireResponseRequest {
    private int pid;
    private String qnName;
    private List<AnswerResponse> answers;
}
