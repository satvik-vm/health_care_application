package com.example.demo.models;

import com.example.demo.dto.DoctorQuestionDTO;
import lombok.Data;

import java.util.List;

@Data
public class FollowUpRequest {
    int id;
    String type;
    String timestamp;
    PrescriptionRequest prescription;
    DoctorQuestionDTO doctorQuestion;
    AppointmentRequest appointment;
    String status;
}
