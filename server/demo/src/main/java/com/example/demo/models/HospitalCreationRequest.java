package com.example.demo.models;

import lombok.Data;

@Data
public class HospitalCreationRequest {
    private String district;
    private String subDivision;
    private UserRequest user;
}