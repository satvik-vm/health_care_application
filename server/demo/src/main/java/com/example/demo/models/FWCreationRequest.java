package com.example.demo.models;

import lombok.Data;

@Data
public class FWCreationRequest {
    private int sup_id;
    private String area;
    private UserRequest user;
}
