package com.example.demo.controllers;

import com.example.demo.Entity.Supervisor;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.models.ModifyUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/supervisor")
public class SupervisorController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/hello")
    public String helloWorld()
    {
        return "Hello, Mukul !!";
    }

    @PostMapping("/modifyDetails")
    public ResponseEntity<User> modifyDetails(@RequestBody ModifyUserRequest request)
    {
        try
        {
            User user = userRepository.getUserByUsername(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setDob(request.getDob());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            System.out.println(request.getPassword());
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
