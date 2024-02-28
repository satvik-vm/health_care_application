package com.example.demo.models;

import lombok.Data;

@Data
public class ModifyUserRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String dob;
    private String phone;
}
