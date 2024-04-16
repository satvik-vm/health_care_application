package com.example.demo.models;


import com.example.demo.Entity.User;
import lombok.Data;

@Data
public class SupervisorCreationRequest {
    private DistrictRequest district;
    private String state;
    private UserRequest user;
}
