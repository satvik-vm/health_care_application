package com.example.demo.dto;

import lombok.Data;

@Data
public class PatientDTO {
    private int publicId;
    private String aabhaId;
    private String firstName;
    private String lastName;
    private String status;
}
