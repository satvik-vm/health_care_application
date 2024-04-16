package com.example.demo.models;

import lombok.Data;

@Data
public class DoctorCreationRequest {
    private UserRequest user;
    private String fullName;
    private String regNo;
}
