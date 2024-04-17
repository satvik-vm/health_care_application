package com.example.demo.models;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DescriptiveAnswerRequest {
    private String qid;
    private String pid;
    private MultipartFile audio;
}
