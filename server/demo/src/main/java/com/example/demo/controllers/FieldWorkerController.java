package com.example.demo.controllers;


import com.example.demo.Entity.Answer;
import com.example.demo.Entity.Patient;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.models.*;
import com.example.demo.services.EmailSenderService;
import com.example.demo.services.FwService;
import com.example.demo.services.GoogleDriveService;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/fw")
@CrossOrigin(origins = "*")
public class FieldWorkerController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    @Autowired
    EmailSenderService emailSenderService;

    @Autowired
    FwService fwService;

    @Autowired
    GoogleDriveService googleDriveService;

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

    @PostMapping("/sendOtp")
    public boolean sendOtp(@RequestBody SendOtpRequest request) {
        System.out.println("Hello World");
        String otp = userService.generateOTP();
        System.out.println(otp);
        String subject = "OTP Verification";
        String body = "Dear Field Worker,\n\n"
                + "Thank you for using our service. To complete your registration/authentication process, please use the following OTP (One-Time Password):\n\n"
                + "OTP: " + otp + "\n\n"
                + "Please enter this OTP on the verification page to confirm your identity.\n\n"
                + "If you did not request this OTP, please ignore this email.\n\n"
                + "Thank you,\n"
                + "Medimate India";
        try {
            userService.createOTP(request.getEmail(), otp);
            emailSenderService.sendEmail(request.getEmail(), subject, body);
            return true; // Email sent successfully
        }
        catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging purposes
            return false; // Failed to send email
        }
    }

    @PostMapping("/verifyOtp")
    public boolean verifyOtp(@RequestBody VerifyOtpRequest request)
    {
        String otp_num = request.getOtp_num();
        User user = userRepository.getUserByUsername(request.getEmail());

        // Check if user or OTP is null
        if (user == null || user.getOtp() == null) {
            return false;
        }

        String stored_otp = user.getOtp().getOtp_num();
        Date expDate = user.getOtp().getExpDate();

        // Check if the provided OTP matches the stored OTP
        if (!otp_num.equals(stored_otp)) {
            return false;
        }

        // Check if the current time is before the expiry time
        Date currentTime = new Date();
        if (currentTime.after(expDate)) {
            // OTP has expired
            return false;
        }

        // OTP is valid and not expired
        return true;
    }

    @GetMapping("/dob")
    public Boolean getDob(Principal principal){
        String email = principal.getName();
        if(userService.getDobByEmail(email) == null) {
            return false;
        }
        return true;
    }

    @PostMapping("/regPatient")
    public Patient regPatient(@RequestBody PatientCreationRequest request)
    {
        return fwService.createPatient(request);
    }

    @GetMapping("/qLogic")
    public String getCategorization(@RequestBody List<Answer> answers)
    {
        return fwService.getCategorizedClass(answers);
    }

    @PostMapping("/uploadDescMsg")
    public Object handleFileUpload(@RequestParam("audio") MultipartFile file) throws IOException, GeneralSecurityException {
        if (file.isEmpty()) {
            return "File is empty";
        }
        File tempFile = File.createTempFile("temp", null);
        file.transferTo(tempFile);
        DriveResponse res = googleDriveService.uploadDescriptiveMsgToDrive(tempFile);
        System.out.println(res);
        return res;
    }
}
