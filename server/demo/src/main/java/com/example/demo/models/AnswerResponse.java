package com.example.demo.models;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AnswerResponse {
    private String mcqAns;
    private int rangeAns;
    private int qid;
}