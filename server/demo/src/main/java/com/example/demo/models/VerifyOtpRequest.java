package com.example.demo.models;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otp_num;
}
