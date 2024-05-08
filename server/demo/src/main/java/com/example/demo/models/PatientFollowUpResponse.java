package com.example.demo.models;

import com.example.demo.dto.AnswerDTO;
import jakarta.persistence.Lob;
import lombok.Data;

import java.util.List;

@Data
public class PatientFollowUpResponse {
    int id;

    String timestamp;

    @Lob
    String answer;
}
