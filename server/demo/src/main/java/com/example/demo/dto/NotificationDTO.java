package com.example.demo.dto;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Data
public class NotificationDTO {
    private String to;
    private String text;
}
