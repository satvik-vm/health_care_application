package com.example.demo.models;

import com.example.demo.Entity.Doctor;
import lombok.Data;

import java.util.Date;

@Data
public class FollowUpRequest {
    int id;
    String type;
    String timestamp;
    PrescriptionRequest prescription;
    DoctorQuestionnaireRequest doctorQuestionnaire;
    AppointmentRequest appointment;
}
