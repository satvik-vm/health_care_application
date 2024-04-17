package com.example.demo.models;

import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireResponseRequest {
    private String pid;
    private List<AnswerResponse> answers;
}
