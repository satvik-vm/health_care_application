package com.example.demo.dto;

import lombok.Data;

@Data
public class Message {
    private String messageContent;
    private String to;
    private String senderName;
    private String receiverName;
    String date;
    String time;
}
