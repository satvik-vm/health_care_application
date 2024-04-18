package com.example.demo.models;

import lombok.Data;

@Data
public class SupervisorCreationRequest {
    private DistrictRequest district;
    private String state;
    private UserRequest user;
}
