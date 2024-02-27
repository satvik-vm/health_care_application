package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/supervisor")
public class SupervisorController {

    @GetMapping("/hello")
    public String helloWorld()
    {
        return "Hello, Mukul !!";
    }
}
