package com.example.demo.services;

import com.example.demo.Entity.Otp;
import com.example.demo.Entity.Role;
import com.example.demo.Entity.User;
import com.example.demo.Repository.OtpRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class OtpService {
    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

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
