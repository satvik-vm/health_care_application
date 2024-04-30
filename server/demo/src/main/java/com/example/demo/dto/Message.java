package com.example.demo.dto;

import lombok.Data;

@Data
public class Message {
    private String messageContent;
    private String to;
    String date;
    String time;
}
