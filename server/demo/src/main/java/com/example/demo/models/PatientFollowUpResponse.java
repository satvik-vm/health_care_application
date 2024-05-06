package com.example.demo.models;

import com.example.demo.dto.AnswerDTO;
import lombok.Data;

import java.util.List;

@Data
public class PatientFollowUpResponse {
    int id;
    List<AnswerDTO> answers;
}
