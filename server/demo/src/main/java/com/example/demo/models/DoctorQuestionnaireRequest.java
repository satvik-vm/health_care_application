package com.example.demo.models;

import com.example.demo.dto.DoctorQuestionDTO;
import lombok.Data;

import java.util.List;

@Data
public class DoctorQuestionnaireRequest {
    List<DoctorQuestionDTO> doctorQuestions;
}
