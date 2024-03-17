package com.example.demo.services;

import com.example.demo.Entity.Admin;
import com.example.demo.Entity.Otp;
import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Repository.OtpRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

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



    public User createUser(String email, String password, Role role)
    {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
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
        int length = 6;
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
        User user = userRepository.getUserByUsername(email);
        otpService.setOrCreateOtp(user, otp_num);
    }

    public String getDobByEmail(String email) {
        User user = userRepository.getUserByUsername(email);
        return user.getDob();
    }
}
