package com.example.demo.dto;


import lombok.Data;

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
}
