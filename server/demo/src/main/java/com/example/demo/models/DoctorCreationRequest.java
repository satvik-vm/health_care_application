package com.example.demo.models;

import lombok.Data;

@Data
public class DoctorCreationRequest {
    private UserRequest user;
    private String firstName;
    private String lastName;
    private String speciality;
    private String status;
}
