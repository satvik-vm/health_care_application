package com.example.demo.services;

import com.example.demo.Entity.IdMapping;
import com.example.demo.Entity.Otp;
import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Repository.IdMappingRepository;
import com.example.demo.Repository.OtpRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class OtpService {
    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdMappingRepository idMappingRepository;

    @Transactional
    public void setOrCreateOtp(User user, String otp_num) {
        Otp otp;
        if(user.getOtp() != null)
        {
            otp = user.getOtp();

        }
        else
        {
            otp = new Otp();

            // Create a new IdMapping object and set its privateId to the Otp's UUID
            IdMapping idMapping = new IdMapping();
            idMapping.setPrivateId(UUID.fromString(otp.getId()));

            // Save the IdMapping object to the database
            idMappingRepository.save(idMapping);// Create a new IdMapping object and set its privateId to the generated UUID

        }
        otp.setOtp_num(otp_num);
        long currentTimeMillis = System.currentTimeMillis();
        long expiryTimeMillis = currentTimeMillis + (15 * 60 * 1000); // 15 minutes in milliseconds
        Date expDate = new Date(expiryTimeMillis);
        otp.setExpDate(expDate);
        otpRepository.save(otp);
        user.setOtp(otp);
        userRepository.save(user);
    }
}
