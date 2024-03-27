package com.example.demo.controllers;

import com.example.demo.Entity.Admin;
import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.models.AdminCreationRequest;
import com.example.demo.models.JwtRequest;
import com.example.demo.models.JwtResponse;
import com.example.demo.security.JwtHelper;
import com.example.demo.services.AdminService;
import com.example.demo.services.CustomUserDetailsService;
import com.example.demo.services.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager manager;


    @Autowired
    private JwtHelper helper;

    @Autowired
    AdminService adminService;

    @Autowired
    UserService userService;

    private Logger logger = LoggerFactory.getLogger(AuthController.class);


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {
        System.out.println(request.getEmail()+ " " + request.getPassword());
        this.doAuthenticate(request.getEmail(), request.getPassword());


        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String role = userDetailsService.loadRoleByUsername(request.getEmail());

        System.out.println(userDetails + "hello");
        String token = this.helper.generateToken(userDetails);
        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .username(userDetails.getUsername())
                .role(role).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            manager.authenticate(authentication);


        }
        catch (AuthenticationException e) {
            // General authentication exception occurred, log the exception stack trace
            System.err.println("Authentication failed for user: " + email);
            e.printStackTrace(); // Log the stack trace
            throw e; // Re-throw the exception
        }

    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }

    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin(@RequestBody AdminCreationRequest request)
    {

            String email = request.getUser().getEmail();

            String password = request.getUser().getPassword();

            String roleName = request.getUser().getRole().getName();
            // Create a new Role object
            Role role = new Role();
            role.setName(roleName);


            User user = userService.createUser(email, password, role);



            // Create a new Admin object
            Admin admin = new Admin();
            admin.setUser(user);


            // Save the Admin object
            adminService.createAdmin(admin);

            return ResponseEntity.ok("Admin created successfully");
    }

}
