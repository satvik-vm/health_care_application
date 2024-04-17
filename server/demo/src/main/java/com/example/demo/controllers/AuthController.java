package com.example.demo.controllers;

import com.example.demo.Entity.Admin;
import com.example.demo.Entity.IdMapping;
import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.models.AdminCreationRequest;
import com.example.demo.models.JwtRequest;
import com.example.demo.models.JwtResponse;
import com.example.demo.security.JwtHelper;
import com.example.demo.security.TokenBlacklist;
import com.example.demo.services.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;

import java.util.UUID;

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

    @Autowired
    RoleService roleService;

    @Autowired
    TokenBlacklist tokenBlacklist;

    @Autowired
    private IdMappingRepository idMappingRepository;

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
            Role role = roleService.getOrCreateRole(roleName);
            role.setName(roleName);


            User user = userService.createUser(email, password, role);



            // Create a new Admin object
            Admin admin = new Admin();

            // Create a new IdMapping object and set its privateId to the Admin's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(admin.getUniqueId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

            admin.setUser(user);

            // Save the Admin object
            adminService.createAdmin(admin);

            return ResponseEntity.ok("Admin created successfully");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        tokenBlacklist.addToBlacklist(token);

        // Clear any session-related data if necessary

        return ResponseEntity.ok("Logged out successfully");
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        // Get the Authorization header from the request
        String requestHeader = request.getHeader("Authorization");

        // Check if the Authorization header is not null and starts with "Bearer "
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            // Extract the JWT token (remove "Bearer " prefix)
            return requestHeader.substring(7);
        }

        // If the Authorization header is not valid, return null
        return null;
    }

}
