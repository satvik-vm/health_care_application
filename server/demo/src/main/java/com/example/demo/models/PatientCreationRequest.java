package com.example.demo.models;

import lombok.Data;

@Data
public class PatientCreationRequest {
    private String aabha;
    private String firstName;
    private String lastName;
    private String address;
    private String gender;
    private String dob;
    private boolean assist;
    private String email;
    private String phone;
    private RoleRequest role;
}
