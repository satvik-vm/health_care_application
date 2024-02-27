package com.example.demo.models;

import lombok.Data;

@Data
public class UserRequest {
    private String email;
    private String password;
    private RoleRequest role;
}
