package com.example.demo.models;

import lombok.Data;

@Data
public class PrescriptionRequest {
    String medicine;
    String test;
    String precaution;

    int days;
}
