package com.example.demo.dto;


import lombok.Data;

@Data
public class ProfileDTO {
    int id;
    String name;
    LastMsgDTO data;
}
