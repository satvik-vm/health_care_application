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
    int pId;
}
