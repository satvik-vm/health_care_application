package com.example.demo.models;

import jakarta.persistence.Lob;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AnswerResponse {
    private String mcqAns;

    @Lob
    private String subjAns;

    private int rangeAns;
    private int qid;
}
