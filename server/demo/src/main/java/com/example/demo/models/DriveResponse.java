package com.example.demo.models;

import lombok.Data;

@Data
public class DriveResponse {
    private int status;
    private String msg;
    private String url;
}
