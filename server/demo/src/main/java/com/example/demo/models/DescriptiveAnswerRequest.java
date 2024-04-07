package com.example.demo.models;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DescriptiveAnswerRequest {
    private int qid;
    private int pid;
    private MultipartFile audio;
}
