package com.example.demo.models;

import lombok.Data;

@Data
public class FWCreationRequest {
    private String area;
    private String state;
    private UserRequest user;
}
