package com.example.demo.models;


import lombok.Data;

import java.time.Duration;

@Data
public class TaskCreationRequest {
    String task_type;
    String description;
    String date;
    String time;
    Duration duration;
    String medicine;
    String test;
    String precaution;
    int days;

    String appointment;

    String question;

    int pId;
}
