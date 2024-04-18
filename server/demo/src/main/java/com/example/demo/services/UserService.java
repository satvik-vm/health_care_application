package com.example.demo.services;

import com.example.demo.Entity.*;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.Repository.OtpRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    OtpService otpService;

    @Autowired
    private IdMappingRepository idMappingRepository;



    public User createUser(String email, String password, Role role)
    {
        User user = new User();

        // Create a new IdMapping object and set its privateId to the User's UUID
        IdMapping idMapping = new IdMapping();
        idMapping.setPrivateId(UUID.fromString(user.getUniqueId()));

        // Save the IdMapping object to the database
        idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User createUserForDoctor(String email, String password, Role role, String fname, String lname)
    {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setFirstName(fname);
        user.setLastName(lname);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String generatePassword() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int length = 8; // Desired length of the password
        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return password.toString();
    }


    public String generateOTP()
    {
        int length = 4;
        String numbers = "0123456789";
        StringBuilder otp = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(numbers.length());
            otp.append(numbers.charAt(index));
        }
        return otp.toString();
    }

    public void createOTP(String email, String otp_num)
    {
        try{
            User user = userRepository.getUserByUsername(email);
            otpService.setOrCreateOtp(user, otp_num);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDobByEmail(String email) {
        User user = userRepository.getUserByUsername(email);
        return user.getDob();
    }
}
