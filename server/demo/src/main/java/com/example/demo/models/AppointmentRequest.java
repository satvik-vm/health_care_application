package com.example.demo.models;

import lombok.Data;

import java.time.Duration;

@Data
public class AppointmentRequest {
    Duration duration;
    String date;
    String time;
}
