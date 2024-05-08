package com.example.demo.dto;


import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    int id;
    int pid;
    String type;
    LocalDateTime assignedTime;
    LocalDateTime deadline;
    String date;
    String time;
    boolean status;
    String description;
    String medicine;
    String test;
    String precaution;
    int days;

    String appointment;

    String question;

    String address;
    String name;
}
