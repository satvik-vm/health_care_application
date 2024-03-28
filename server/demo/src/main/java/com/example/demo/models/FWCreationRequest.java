package com.example.demo.models;

import lombok.Data;

@Data
public class FWCreationRequest {
    private DistrictRequest district;
    private String area;
    private UserRequest user;
}
