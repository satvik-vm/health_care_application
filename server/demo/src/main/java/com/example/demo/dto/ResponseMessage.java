package com.example.demo.dto;

import lombok.Data;

@Data
public class ResponseMessage {
    private Message message;
    private String from;

    public ResponseMessage() {
    }

    public ResponseMessage(Message message, String from) {
        this.message = message;
        this.from = from;
    }
}
